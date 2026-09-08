package com.translatr.observability;

import com.translatr.auth.AccessTokenSecurityIdentity;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * For {@code /api/**} requests: stamps observability span attributes on the way in, and
 * increments {@code translatr.apikey.requests} on the way out for key-authenticated calls.
 * The generic {@code http_server_requests} meter (with {@code auth_type}, see
 * {@link AuthTypeTagsContributor}) already covers overall API volume; this adds the per-key
 * dimension without inflating that meter.
 *
 * <h2>Endpoint label is always a JAX-RS template</h2>
 * The translatr resources are {@code *Resource implements *Api}, where the {@code @Path}
 * annotations live on the generated {@code *Api} interface ({@code @Path("/api")} on the type,
 * sub-paths like {@code @Path("/project/{id}")} on the methods) and are <em>not</em> inherited
 * onto the concrete class/method that {@link ResourceInfo} hands back. {@link #templatedPath()}
 * therefore walks the resource type hierarchy (class, superclasses, all interfaces) to recover
 * the class- and method-level {@code @Path} values, so {@code endpoint} is e.g.
 * {@code /api/project/{id}} — never a raw id/UUID. A sanitising fallback replaces any id-shaped
 * segment with {@code {id}} should the reflective lookup ever miss.
 *
 * <h2>Key-auth detection</h2>
 * {@code @Inject SecurityIdentity} is a CDI client proxy, so {@code instanceof
 * AccessTokenSecurityIdentity} does not match even when the delegate is one (see
 * {@code CurrentUserResolver}). Detection mirrors {@code AuthTypeTagsContributor}: match the
 * concrete type when visible, otherwise fall back to an attribute only
 * {@link AccessTokenSecurityIdentity} populates. The counter's {@code key_id} tag comes from
 * {@link AccessTokenSecurityIdentity#KEY_ID_ATTRIBUTE}, which the proxy delegates.
 */
@Provider
@Priority(Priorities.USER)
public class ApiMetricsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /** A path segment that looks like a raw id (long hex/UUID run, or a long/short numeric id). */
    private static final Pattern ID_SEGMENT = Pattern.compile("[0-9a-fA-F-]{8,}|\\d+");
    /** The raw-id shape the ApiMetricsFilterTest gate forbids anywhere in {@code endpoint}. */
    private static final Pattern RAW_ID_IN_PATH = Pattern.compile("/[0-9a-fA-F-]{8,}");

    @Inject MeterRegistry registry;
    @Inject SecurityIdentity identity;
    @Context ResourceInfo resourceInfo;
    @Context UriInfo uriInfo;

    /**
     * Read as a plain property (not via {@link com.translatr.config.TranslatrConfig}) because JAX-RS
     * provider instances are created during RESTEasy Reactive deployment setup, before
     * {@code @ConfigMapping} beans resolve ({@code SRCFG00027}). The matching
     * {@code translatr.observability.apikey-metrics.key-id-label} entry on {@code TranslatrConfig}
     * exists only to keep it a known key under the validated {@code translatr} mapping prefix.
     */
    @ConfigProperty(name = "translatr.observability.apikey-metrics.key-id-label", defaultValue = "true")
    boolean keyIdLabel;

    @Override
    public void filter(ContainerRequestContext req) {
        if (!isApi(req)) {
            return;
        }
        Span span = Span.current();
        if (!span.getSpanContext().isValid()) {
            // OTel SDK dormant (the default) — nothing to stamp.
            return;
        }

        String keyId = identity.getAttribute(AccessTokenSecurityIdentity.KEY_ID_ATTRIBUTE);
        span.setAttribute("user.id",
                identity == null || identity.isAnonymous() || identity.getPrincipal() == null
                        ? "anonymous" : identity.getPrincipal().getName());
        span.setAttribute("translatr.auth_provider", authProvider());
        if (keyId != null) {
            span.setAttribute("translatr.key_id", keyId);
        }

        String projectId = projectId(req);
        if (projectId != null) {
            span.setAttribute("translatr.project_id", projectId);
        }
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) {
        if (!isApi(req)) {
            return;
        }
        if (!isAccessKey(identity)) {
            return;
        }
        String keyId = identity.getAttribute(AccessTokenSecurityIdentity.KEY_ID_ATTRIBUTE);
        if (keyId == null) {
            return;
        }

        String endpoint = templatedPath();
        String status = String.valueOf(resp.getStatus());

        if (keyIdLabel) {
            registry.counter("translatr.apikey.requests",
                    "key_id", keyId, "endpoint", endpoint, "status", status).increment();
        } else {
            registry.counter("translatr.apikey.requests",
                    "endpoint", endpoint, "status", status).increment();
        }
    }

    private boolean isApi(ContainerRequestContext req) {
        String p = req.getUriInfo().getPath();
        if (p == null) {
            return false;
        }
        return p.equals("api") || p.equals("/api")
                || p.startsWith("api/") || p.startsWith("/api/");
    }

    private boolean isAccessKey(SecurityIdentity id) {
        if (id == null) {
            return false;
        }
        return id instanceof AccessTokenSecurityIdentity
                || id.getAttribute(AccessTokenSecurityIdentity.USER_ATTRIBUTE) != null;
    }

    private String authProvider() {
        if (isAccessKey(identity)) {
            return "access-key";
        }
        if (identity == null || identity.isAnonymous()) {
            return "none";
        }
        return "oidc";
    }

    private String projectId(ContainerRequestContext req) {
        String direct = req.getUriInfo().getPathParameters().getFirst("projectId");
        if (direct != null && !direct.isBlank()) {
            return direct;
        }
        // Project routes carry the id as {id} on a /project path (see ProjectsApi).
        if (templatedPath().contains("/project")) {
            String id = req.getUriInfo().getPathParameters().getFirst("id");
            if (id != null && !id.isBlank()) {
                return id;
            }
        }
        return null;
    }

    /** The matched JAX-RS template, e.g. {@code "/api/project/{id}"} — never a raw id/UUID. */
    private String templatedPath() {
        Class<?> resourceClass = resourceInfo != null ? resourceInfo.getResourceClass() : null;
        Method resourceMethod = resourceInfo != null ? resourceInfo.getResourceMethod() : null;

        String built = "";
        if (resourceClass != null && resourceMethod != null) {
            built = norm(classTemplate(resourceClass))
                    + norm(methodTemplate(resourceClass, resourceMethod));
        }
        if (built.isEmpty() || RAW_ID_IN_PATH.matcher(built).find()) {
            built = sanitize("/" + (uriInfo != null ? uriInfo.getPath() : ""));
        }
        return built.isEmpty() ? "/" : built;
    }

    /** First {@code @Path} found on the class, its superclasses, or any implemented interface. */
    private static String classTemplate(Class<?> c) {
        for (Class<?> t : hierarchy(c)) {
            jakarta.ws.rs.Path p = t.getAnnotation(jakarta.ws.rs.Path.class);
            if (p != null) {
                return p.value();
            }
        }
        return "";
    }

    /** {@code @Path} on the same method signature declared anywhere in the type hierarchy. */
    private static String methodTemplate(Class<?> c, Method m) {
        for (Class<?> t : hierarchy(c)) {
            try {
                Method candidate = t.getDeclaredMethod(m.getName(), m.getParameterTypes());
                jakarta.ws.rs.Path p = candidate.getAnnotation(jakarta.ws.rs.Path.class);
                if (p != null) {
                    return p.value();
                }
            } catch (NoSuchMethodException ignored) {
                // not declared on this type — try the next one
            }
        }
        return "";
    }

    private static List<Class<?>> hierarchy(Class<?> c) {
        Set<Class<?>> seen = new LinkedHashSet<>();
        Deque<Class<?>> queue = new ArrayDeque<>();
        queue.add(c);
        while (!queue.isEmpty()) {
            Class<?> cur = queue.poll();
            if (cur == null || cur == Object.class || !seen.add(cur)) {
                continue;
            }
            if (cur.getSuperclass() != null) {
                queue.add(cur.getSuperclass());
            }
            for (Class<?> i : cur.getInterfaces()) {
                queue.add(i);
            }
        }
        return new ArrayList<>(seen);
    }

    private static String norm(String s) {
        if (s == null || s.isEmpty() || s.equals("/")) {
            return "";
        }
        String v = s.startsWith("/") ? s : "/" + s;
        return v.endsWith("/") ? v.substring(0, v.length() - 1) : v;
    }

    /** Rebuild a path, replacing any id-shaped segment with {@code {id}}. */
    private static String sanitize(String path) {
        StringBuilder sb = new StringBuilder();
        for (String seg : path.split("/")) {
            if (seg.isEmpty()) {
                continue;
            }
            sb.append('/').append(ID_SEGMENT.matcher(seg).matches() ? "{id}" : seg);
        }
        return sb.length() == 0 ? "" : sb.toString();
    }
}
