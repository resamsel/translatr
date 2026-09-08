package com.translatr.observability;

import com.translatr.auth.AccessTokenSecurityIdentity;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.quarkus.micrometer.runtime.HttpServerMetricsTagsContributor;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Singleton;

/**
 * Adds an {@code auth_type} tag to every {@code http_server_requests} meter so the SigNoz
 * dashboard can split HTTP traffic by authentication mechanism.
 *
 * <p>Bounded to three low-cardinality values:
 * <ul>
 *   <li>{@code access-key} &mdash; request carried a valid API access token
 *       ({@link AccessTokenSecurityIdentity}, from Task 3)</li>
 *   <li>{@code session}    &mdash; any other non-anonymous identity (OIDC / form session)</li>
 *   <li>{@code anonymous}  &mdash; no identity, an anonymous identity, or the identity could
 *       not be resolved from the completed request</li>
 * </ul>
 *
 * <h2>How the identity is reached</h2>
 * {@link HttpServerMetricsTagsContributor} is a Quarkus 3.32 SPI: a CDI bean implementing it
 * contributes tags to the {@code http.server.requests} timer. Its single method is
 * {@code Tags contribute(Context)}, and {@code Context} exposes {@code request()} (the Vert.x
 * {@link io.vertx.core.http.HttpServerRequest}), {@code response()} and
 * {@link Context#requestContextLocalData(Object)}.
 *
 * <p>{@code contribute} is invoked from {@code VertxHttpServerMetrics.responseEnd()} <em>after</em>
 * the response has ended, when the request scope / {@code CurrentVertxRequest} are already torn
 * down &mdash; so the {@link RoutingContext} cannot be injected. It is instead read back from the
 * Vert.x duplicated-context local {@code "quarkus.http.routing.context"}
 * ({@code io.quarkus.vertx.http.runtime.security.HttpSecurityUtils.ROUTING_CONTEXT_ATTRIBUTE}),
 * which the Quarkus {@code HttpSecurityRecorder.AuthenticationHandler} populates for every request
 * when {@code quarkus.http.auth.propagate-security-identity=true} (set in application.properties).
 * That local survives the request, which is exactly what
 * {@link Context#requestContextLocalData(Object)} is designed to expose. From the
 * {@link RoutingContext} the {@link SecurityIdentity} is the one attached by the auth layer as a
 * {@link QuarkusHttpUser}.
 */
@Singleton
public class AuthTypeTagsContributor implements HttpServerMetricsTagsContributor {

    static final String TAG_KEY   = "auth_type";
    static final String ACCESS_KEY = "access-key";
    static final String SESSION    = "session";
    static final String ANONYMOUS  = "anonymous";

    /**
     * Vert.x duplicated-context local key under which the Quarkus HTTP security layer stashes the
     * current {@link RoutingContext} (value of
     * {@code io.quarkus.vertx.http.runtime.security.HttpSecurityUtils.ROUTING_CONTEXT_ATTRIBUTE}).
     */
    static final String ROUTING_CONTEXT_LOCAL_KEY = "quarkus.http.routing.context";

    @Override
    public Tags contribute(Context context) {
        return Tags.of(Tag.of(TAG_KEY, resolveAuthType(context)));
    }

    private String resolveAuthType(Context context) {
        try {
            RoutingContext routingContext = context.requestContextLocalData(ROUTING_CONTEXT_LOCAL_KEY);
            if (routingContext == null) {
                return ANONYMOUS;
            }
            User user = routingContext.user();
            if (!(user instanceof QuarkusHttpUser quarkusHttpUser)) {
                return ANONYMOUS;
            }
            SecurityIdentity identity = quarkusHttpUser.getSecurityIdentity();
            if (identity == null || identity.isAnonymous()) {
                return ANONYMOUS;
            }
            return isAccessToken(identity) ? ACCESS_KEY : SESSION;
        } catch (RuntimeException e) {
            // Never let tag resolution break metrics recording.
            return ANONYMOUS;
        }
    }

    /**
     * A {@code SecurityIdentityAugmentor} may wrap the raw identity, so mirror
     * {@code CurrentUserResolver}: match the concrete type when it is visible, otherwise fall back
     * to the attribute that only {@link AccessTokenSecurityIdentity} populates.
     */
    private boolean isAccessToken(SecurityIdentity identity) {
        return identity instanceof AccessTokenSecurityIdentity
                || identity.getAttribute(AccessTokenSecurityIdentity.USER_ATTRIBUTE) != null;
    }
}
