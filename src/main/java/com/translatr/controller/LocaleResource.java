package com.translatr.controller;

import com.translatr.criteria.LocaleCriteria;
import com.translatr.dto.LocaleDto;
import com.translatr.dto.PagedList;
import com.translatr.service.LocaleService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocaleResource {

    @Inject LocaleService localeService;

    @GET  @Path("/project/{projectId}/locales") @PermitAll
    public PagedList<LocaleDto> findByProject(@PathParam("projectId") UUID projectId,
                                              @BeanParam LocaleCriteria criteria) {
        criteria.projectId = projectId;
        return localeService.find(criteria);
    }

    @GET  @Path("/locale/{id}")     @PermitAll
    public LocaleDto get(@PathParam("id") UUID id) { return localeService.get(id); }

    @POST @Path("/locale")          @Authenticated
    public LocaleDto create(LocaleDto dto) { return localeService.create(dto); }

    @PUT  @Path("/locale")          @Authenticated
    public LocaleDto update(LocaleDto dto) { return localeService.update(dto); }

    @DELETE @Path("/locale/{id}")   @Authenticated
    public LocaleDto delete(@PathParam("id") UUID id) { return localeService.delete(id); }
}
