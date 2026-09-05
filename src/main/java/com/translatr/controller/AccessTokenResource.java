package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.AccessTokenCriteria;
import com.translatr.dto.AccessTokenDto;
import com.translatr.dto.AccessTokenPayload;
import com.translatr.dto.PagedAccessTokenList;
import com.translatr.dto.PagedList;
import com.translatr.generated.api.AccessTokensApi;
import com.translatr.service.AccessTokenService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    public AccessTokenPayload getAccessToken(Long id) {
        return toApiDto(tokenService.get(id));
    }

    @Override
    public AccessTokenPayload createAccessToken(AccessTokenPayload accessToken) {
        var owner = currentUserResolver.resolve();
        return toApiDto(tokenService.create(toServiceDto(accessToken), owner));
    }

    @Override
    public AccessTokenPayload updateAccessToken(AccessTokenPayload accessToken) {
        return toApiDto(tokenService.update(toServiceDto(accessToken)));
    }

    @Override
    public AccessTokenPayload deleteAccessToken(Long id) {
        return toApiDto(tokenService.delete(id));
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
                src.total, src.offset, src.limit, src.hasNext, src.hasPrev,
                src.list.stream().map(AccessTokenResource::toApiDto).toList());
    }

    private static AccessTokenPayload toApiDto(AccessTokenDto d) {
        return new AccessTokenPayload()
                .id(d.id)
                .whenCreated(toOffsetDateTime(d.whenCreated))
                .whenUpdated(toOffsetDateTime(d.whenUpdated))
                .userId(d.userId)
                .userUsername(d.userUsername)
                .name(d.name)
                .key(d.key)
                .scope(d.scope);
    }

    /**
     * A just-persisted {@link AccessTokenDto} (from {@code AccessTokenService.create}) can still have
     * a null {@code whenCreated}/{@code whenUpdated} here: {@code @CreationTimestamp}/{@code
     * @UpdateTimestamp} are populated by Hibernate at flush time, which hasn't happened yet when the
     * entity is mapped back immediately after {@code persist()}.
     */
    private static OffsetDateTime toOffsetDateTime(Instant i) {
        return i == null ? null : i.atOffset(ZoneOffset.UTC);
    }

    private static AccessTokenDto toServiceDto(AccessTokenPayload a) {
        AccessTokenDto d = new AccessTokenDto();
        d.id    = a.getId();
        d.name  = a.getName();
        d.scope = a.getScope();
        return d;
    }
}
