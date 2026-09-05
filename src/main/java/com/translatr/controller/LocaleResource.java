package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
import com.translatr.criteria.LocaleCriteria;
import com.translatr.dto.LocaleDto;
import com.translatr.dto.PagedLocaleList;
import com.translatr.dto.PagedList;
import com.translatr.generated.api.LocalesApi;
import com.translatr.service.LocaleService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.UUID;

public class LocaleResource implements LocalesApi {

    private final LocaleService       localeService;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public LocaleResource(LocaleService localeService, CurrentUserResolver currentUserResolver) {
        this.localeService       = localeService;
        this.currentUserResolver = currentUserResolver;
    }

    /**
     * The language a locale's {@code displayName} should be rendered in: the signed-in user's
     * preferred language, or English for an anonymous caller / a user who never picked one.
     */
    private java.util.Locale viewerLocale() {
        return currentUserResolver.resolveOptional()
                .map(u -> u.preferredLocale)
                .filter(tag -> tag != null && !tag.isBlank())
                .map(java.util.Locale::forLanguageTag)
                .orElse(java.util.Locale.ENGLISH);
    }

    @Override
    @PermitAll
    public PagedLocaleList findLocalesByProject(UUID projectId, String search, Integer offset, Integer limit,
                                                 String order, String fetch, UUID keyId, Boolean missing,
                                                 String localeName) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, keyId, missing, localeName);
        var paged = localeService.find(criteria, viewerLocale());
        return toPagedDto(paged);
    }

    @Override
    @PermitAll
    public LocaleDto getLocale(UUID id) {
        return localeService.get(id, viewerLocale());
    }

    @Override
    @PermitAll
    public LocaleDto getLocaleByOwnerAndProjectNameAndName(String username, String projectName, String localeName) {
        return localeService.getByOwnerAndProjectNameAndName(username, projectName, localeName, viewerLocale());
    }

    @Override
    @Authenticated
    public LocaleDto createLocale(LocaleDto localeDto) {
        return localeService.create(localeDto);
    }

    @Override
    @Authenticated
    public LocaleDto updateLocale(LocaleDto localeDto) {
        return localeService.update(localeDto);
    }

    @Override
    @Authenticated
    public LocaleDto deleteLocale(UUID id) {
        return localeService.delete(id);
    }

    static LocaleCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                      UUID projectId, UUID keyId, Boolean missing, String localeName) {
        LocaleCriteria c = new LocaleCriteria();
        c.search     = search;
        c.offset     = offset;
        c.limit      = limit;
        c.order      = order;
        c.fetch      = fetch;
        c.projectId  = projectId;
        c.keyId      = keyId;
        c.missing    = missing;
        c.localeName = localeName;
        return c;
    }

    private static PagedLocaleList toPagedDto(PagedList<LocaleDto> src) {
        return new PagedLocaleList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
