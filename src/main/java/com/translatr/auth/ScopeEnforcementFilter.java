package com.translatr.auth;

import com.translatr.service.QuerySupport;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.ConfigProvider;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Enforces {@link RequiresScope} / {@link NoScopeRequired} for access-token requests (#364).
 *
 * <p>Session and anonymous identities pass untouched. Tokens are recognised by
 * {@link AccessTokenSecurityIdentity#SCOPES_ATTRIBUTE}: the injected {@link SecurityIdentity} is a
 * CDI client proxy, so {@code instanceof AccessTokenSecurityIdentity} never matches.
 *
 * <p>An endpoint with neither annotation denies tokens (fail closed). {@code ?fetch=members}
 * additionally requires {@link Scope#MemberRead}, matched with the same semantics as the services.
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class ScopeEnforcementFilter implements ContainerRequestFilter {

    private final MeterRegistry    registry;
    private final SecurityIdentity identity;
    @Context ResourceInfo resourceInfo;
    @Context UriInfo      uriInfo;

    public ScopeEnforcementFilter(MeterRegistry registry, SecurityIdentity identity) {
        this.registry = registry;
        this.identity = identity;
    }

    @Override
    public void filter(ContainerRequestContext ctx) {
        if (identity == null || identity.isAnonymous()) return;
        Set<Scope> granted = identity.getAttribute(AccessTokenSecurityIdentity.SCOPES_ATTRIBUTE);
        if (granted == null) return;

        Method method = resourceInfo.getResourceMethod();
        if (method == null) return; // no matched route — nothing to authorize

        Optional<Set<Scope>> declared = requiredScopes(method, resourceInfo.getResourceClass());
        if (declared.isEmpty()) {
            deny(ctx, List.of());
            return;
        }
        EnumSet<Scope> required = declared.get().isEmpty()
                ? EnumSet.noneOf(Scope.class) : EnumSet.copyOf(declared.get());
        if (QuerySupport.wants(uriInfo.getQueryParameters().getFirst("fetch"), "members"))
            required.add(Scope.MemberRead);

        required.removeAll(granted);
        if (!required.isEmpty())
            deny(ctx, required.stream().map(Scope::value).sorted().toList());
    }

    /**
     * Scopes the resource method declares: empty Optional when neither annotation is present
     * (tokens denied), an empty set for {@link NoScopeRequired}. The method annotation wins over
     * the class one. {@code method} may be the generated interface's; the implementation's
     * same-signature method on {@code resourceClass} is consulted.
     */
    public static Optional<Set<Scope>> requiredScopes(Method method, Class<?> resourceClass) {
        Method impl = method;
        try {
            impl = resourceClass.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException ignored) {
            // fall back to the method as given
        }
        Optional<Set<Scope>> onMethod = declared(impl.getAnnotation(RequiresScope.class),
                impl.isAnnotationPresent(NoScopeRequired.class));
        if (onMethod.isPresent()) return onMethod;
        return declared(resourceClass.getAnnotation(RequiresScope.class),
                resourceClass.isAnnotationPresent(NoScopeRequired.class));
    }

    private static Optional<Set<Scope>> declared(RequiresScope requires, boolean noScope) {
        if (requires != null) {
            EnumSet<Scope> set = EnumSet.noneOf(Scope.class);
            set.addAll(List.of(requires.value()));
            return Optional.of(set);
        }
        return noScope ? Optional.of(EnumSet.noneOf(Scope.class)) : Optional.empty();
    }

    private void deny(ContainerRequestContext ctx, List<String> missing) {
        if (metricsEnabled())
            registry.counter("translatr.apikey.auth.failures", "reason", "scope").increment();
        ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                .header(HttpHeaders.WWW_AUTHENTICATE,
                        "Bearer error=\"insufficient_scope\", scope=\"" + String.join(" ", missing) + "\"")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new InsufficientScopeResponse(missing))
                .build());
    }

    /** Live read, not {@code @ConfigProperty} — see {@code ObservabilityFlags} for why. */
    private static boolean metricsEnabled() {
        return ConfigProvider.getConfig()
                .getOptionalValue("translatr.observability.metrics-enabled", Boolean.class)
                .orElse(false);
    }
}
