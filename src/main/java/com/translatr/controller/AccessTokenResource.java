package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.AccessTokenCriteria;
import com.translatr.dto.AccessTokenDto;
import com.translatr.dto.PagedAccessTokenList;
import com.translatr.dto.PagedList;
import com.translatr.generated.api.AccessTokensApi;
import com.translatr.service.AccessTokenService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;

import java.util.UUID;

@Authenticated
public class AccessTokenResource implements AccessTokensApi {

    private final AccessTokenService  tokenService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public AccessTokenResource(AccessTokenService tokenService, CurrentUserResolver currentUserResolver) {
        this.tokenService        = tokenService;
        this.currentUserResolver = currentUserResolver;
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
    public AccessTokenDto createAccessToken(AccessTokenDto accessToken) {
        var owner = currentUserResolver.resolve();
        return tokenService.create(accessToken, owner);
    }

    @Override
    public AccessTokenDto updateAccessToken(AccessTokenDto accessToken) {
        return tokenService.update(accessToken);
    }

    @Override
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
