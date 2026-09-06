package com.translatr.controller;

import com.translatr.criteria.KeyCriteria;
import com.translatr.dto.KeyDto;
import com.translatr.dto.PagedKeyList;
import com.translatr.dto.PagedList;
import com.translatr.generated.api.KeysApi;
import com.translatr.service.KeyService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import java.util.UUID;

public class KeyResource implements KeysApi {

    private final KeyService keyService;

    @Inject
    public KeyResource(KeyService keyService) {
        this.keyService = keyService;
    }

    @Override
    @PermitAll
    public PagedKeyList findKeysByProject(UUID projectId, String search, Integer offset, Integer limit,
                                           String order, String fetch, UUID localeId, Boolean missing) {
        var criteria = toCriteria(search, offset, limit, order, fetch, projectId, localeId, missing);
        return toPagedDto(keyService.find(criteria));
    }

    @Override
    @PermitAll
    public KeyDto getKey(UUID id) {
        return keyService.get(id);
    }

    @Override
    @PermitAll
    public KeyDto getKeyByOwnerAndProjectNameAndName(String username, String projectName, String keyName) {
        return keyService.getByOwnerAndProjectNameAndName(username, projectName, keyName);
    }

    @Override
    @Authenticated
    public KeyDto createKey(KeyDto keyDto) {
        return keyService.create(keyDto);
    }

    @Override
    @Authenticated
    public KeyDto updateKey(KeyDto keyDto) {
        return keyService.update(keyDto);
    }

    @Override
    @Authenticated
    public KeyDto deleteKey(UUID id) {
        return keyService.delete(id);
    }

    static KeyCriteria toCriteria(String search, Integer offset, Integer limit, String order, String fetch,
                                   UUID projectId, UUID localeId, Boolean missing) {
        KeyCriteria c = new KeyCriteria();
        c.search    = search;
        c.offset    = offset;
        c.limit     = limit;
        c.order     = order;
        c.fetch     = fetch;
        c.projectId = projectId;
        c.localeId  = localeId;
        c.missing   = missing;
        return c;
    }

    private static PagedKeyList toPagedDto(PagedList<KeyDto> src) {
        return new PagedKeyList(src.total, src.offset, src.limit, src.hasNext, src.hasPrev, src.list);
    }
}
