package com.translatr.controller;

import com.translatr.criteria.LocaleCriteria;
import com.translatr.dto.LocaleDto;
import com.translatr.dto.PagedList;
import com.translatr.exporter.ExporterFactory;
import com.translatr.importer.ImportResult;
import com.translatr.importer.ImporterFactory;
import com.translatr.model.Locale;
import com.translatr.repository.LocaleRepository;
import com.translatr.service.LocaleService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.UUID;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocaleResource {

    private final LocaleService    localeService;
    private final LocaleRepository localeRepo;
    private final ImporterFactory  importerFactory;
    private final ExporterFactory  exporterFactory;

    @Inject
    public LocaleResource(LocaleService localeService, LocaleRepository localeRepo,
                          ImporterFactory importerFactory, ExporterFactory exporterFactory) {
        this.localeService   = localeService;
        this.localeRepo      = localeRepo;
        this.importerFactory = importerFactory;
        this.exporterFactory = exporterFactory;
    }

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

    @POST
    @Path("/locale/{localeId}/import/{fileType}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Authenticated
    public ImportResult upload(@PathParam("localeId") UUID localeId,
                               @PathParam("fileType") String fileType,
                               InputStream body) throws Exception {
        Locale locale = localeRepo.findByIdOptional(localeId)
                .orElseThrow(NotFoundException::new);
        return importerFactory.forFileType(fileType).apply(body, locale);
    }

    @GET
    @Path("/locale/{localeId}/export/{fileType}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @PermitAll
    public Response download(@PathParam("localeId") UUID localeId,
                             @PathParam("fileType") String fileType) {
        Locale locale   = localeRepo.findByIdOptional(localeId)
                .orElseThrow(NotFoundException::new);
        var exporter    = exporterFactory.forFileType(fileType);
        byte[] content  = exporter.apply(locale);
        return Response.ok(content)
                .header("Content-Disposition", "attachment; filename=" + exporter.getFilename(locale))
                .header("Content-Type", exporter.getContentType())
                .build();
    }
}
