package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.auth.RequiresScope;
import com.translatr.auth.Scope;
import com.translatr.criteria.AccessTokenCriteria;
import com.translatr.dto.AccessTokenDto;
import com.translatr.dto.PagedAccessTokenList;
import com.translatr.dto.PagedList;
import com.translatr.generated.api.AccessTokensApi;
import com.translatr.service.AccessTokenService;
import com.translatr.auth.AccessTokenSecurityIdentity;
import com.translatr.auth.InsufficientScopeResponse;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Authenticated
@RequiresScope(Scope.AccessTokenRead)
public class AccessTokenResource implements AccessTokensApi {

    private final AccessTokenService  tokenService;
    private final CurrentUserResolver currentUserResolver;

    private final SecurityIdentity    identity;

    @Inject
    public AccessTokenResource(AccessTokenService tokenService, CurrentUserResolver currentUserResolver,
                               SecurityIdentity identity) {
        this.tokenService        = tokenService;
        this.currentUserResolver = currentUserResolver;
        this.identity            = identity;
    }

    @Override
    public PagedAccessTokenList findAccessTokens(String search, Integer offset, Integer limit, String order,
                                                  String fetch, UUID userId) {
        var owner    = currentUserResolver.resolve();
        var criteria = toCriteria(search, offset, limit, order, fetch, userId);
        return toPagedDto(tokenService.find(criteria, owner.id));
    }

    @Override
    public AccessTokenDto getAccessToken(Long id) {
        return tokenService.get(id);
    }

    @Override
    @RequiresScope(Scope.AccessTokenWrite)
    public AccessTokenDto createAccessToken(AccessTokenDto accessToken) {
        requireGrantable(accessToken.getScope());
        var owner = currentUserResolver.resolve();
        return tokenService.create(accessToken, owner);
    }

    @Override
    @RequiresScope(Scope.AccessTokenWrite)
    public AccessTokenDto updateAccessToken(AccessTokenDto accessToken) {
        requireGrantable(accessToken.getScope());
        return tokenService.update(accessToken);
    }

    /**
     * A token-authenticated caller may only grant scopes it holds itself, so it cannot mint a more
     * powerful token. Session callers have no scope set and are not restricted here.
     */
    private void requireGrantable(String requestedScope) {
        // The identity is a CDI client proxy, so read the scopes attribute rather than instanceof.
        Set<Scope> granted = identity.getAttribute(AccessTokenSecurityIdentity.SCOPES_ATTRIBUTE);
        if (granted == null || requestedScope == null) return;
        List<String> missing = Scope.parse(requestedScope).stream()
                .filter(s -> !granted.contains(s))
                .map(Scope::value)
                .toList();
        if (missing.isEmpty()) return;
        throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                .header(HttpHeaders.WWW_AUTHENTICATE,
                        "Bearer error=\"insufficient_scope\", scope=\"" + String.join(" ", missing) + "\"")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new InsufficientScopeResponse(missing))
                .build());
    }

    @Override
    @RequiresScope(Scope.AccessTokenWrite)
    public AccessTokenDto deleteAccessToken(Long id) {
        return tokenService.delete(id);
    }

    static AccessTokenCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                           UUID userId) {
        AccessTokenCriteria c = new AccessTokenCriteria();
        c.search = search;
        c.offset = offset;
        c.limit  = limit;
        c.order  = order;
        c.fetch  = fetch;
        c.userId = userId;
        return c;
    }

    private static PagedAccessTokenList toPagedDto(PagedList<AccessTokenDto> src) {
        return new PagedAccessTokenList(
                src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
