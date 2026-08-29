package com.translatr.controller;

import com.translatr.auth.CurrentUserResolver;
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

    private final LocaleService       localeService;
    private final LocaleRepository    localeRepo;
    private final ImporterFactory     importerFactory;
    private final ExporterFactory     exporterFactory;
    private final CurrentUserResolver currentUserResolver;

    @Inject
    public LocaleResource(LocaleService localeService, LocaleRepository localeRepo,
                          ImporterFactory importerFactory, ExporterFactory exporterFactory,
                          CurrentUserResolver currentUserResolver) {
        this.localeService       = localeService;
        this.localeRepo          = localeRepo;
        this.importerFactory     = importerFactory;
        this.exporterFactory     = exporterFactory;
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

    @GET  @Path("/project/{projectId}/locales") @PermitAll
    public PagedList<LocaleDto> findByProject(@PathParam("projectId") UUID projectId,
                                              @BeanParam LocaleCriteria criteria) {
        criteria.projectId = projectId;
        return localeService.find(criteria, viewerLocale());
    }

    @GET  @Path("/locale/{id}")     @PermitAll
    public LocaleDto get(@PathParam("id") UUID id) { return localeService.get(id, viewerLocale()); }

    @GET  @Path("/{username}/{projectName}/locales/{localeName}")  @PermitAll
    public LocaleDto getByOwnerAndProjectNameAndName(
            @PathParam("username")    String username,
            @PathParam("projectName") String projectName,
            @PathParam("localeName")  String localeName) {
        return localeService.getByOwnerAndProjectNameAndName(username, projectName, localeName, viewerLocale());
    }

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
