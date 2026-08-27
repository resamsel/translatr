package com.translatr.auth;

import com.translatr.model.User;
import com.translatr.service.UserService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
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
        // On a @PermitAll endpoint the request may be anonymous; without this guard the OIDC
        // branch below would call findOrCreate("oidc", null, null, null) and blow up persisting
        // a blank User (500). Return a flat 401 (not Quarkus's UnauthorizedException, which an
        // interactive OIDC setup turns into a 302 login redirect) so XHR callers can handle it.
        if (identity == null || identity.isAnonymous()) {
            throw new NotAuthorizedException("Bearer");
        }

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
