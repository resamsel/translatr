package com.translatr.auth;

import com.translatr.model.User;
import com.translatr.service.UserService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Resolves the authenticated {@link User} regardless of which
 * {@link io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism} handled the
 * request: an access-token identity already carries the resolved user, while an OIDC identity
 * only carries JWT claims and needs {@link UserService#findOrCreate} to look up (or provision)
 * the corresponding local user.
 */
@ApplicationScoped
public class CurrentUserResolver {

    @Inject SecurityIdentity identity;
    @Inject UserService      userService;
    @Inject JsonWebToken     jwt;

    public User resolve() {
        // identity is a CDI client proxy, so `instanceof AccessTokenSecurityIdentity` never
        // matches even when the delegate is one — read the user via the delegated attribute
        // accessor instead, which AccessTokenSecurityIdentity populates.
        User accessTokenUser = identity.getAttribute(AccessTokenSecurityIdentity.USER_ATTRIBUTE);
        if (accessTokenUser != null) {
            return accessTokenUser;
        }
        return userService.findOrCreate("oidc", jwt.getSubject(), jwt.getClaim("name"), jwt.getClaim("email"));
    }
}
