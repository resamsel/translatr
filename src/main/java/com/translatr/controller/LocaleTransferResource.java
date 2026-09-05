package com.translatr.controller;

import com.translatr.exporter.ExporterFactory;
import com.translatr.importer.ImportResult;
import com.translatr.importer.ImporterFactory;
import com.translatr.model.Locale;
import com.translatr.repository.LocaleRepository;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.UUID;

/**
 * Binary import/export for a locale's translations. Kept out of the contract-first
 * openapi.yaml (and thus out of {@code mp.openapi.scan.exclude.classes}): a raw
 * octet-stream body/response with a per-file-type dynamic Content-Disposition header
 * doesn't fit the generated-interface pattern without a generator-wide {@code returnResponse}
 * flag that would also change every other migrated resource's return type. Split into its
 * own class (rather than living on LocaleResource, which implements the generated
 * LocalesApi and must be excluded from annotation scanning) purely so these two endpoints
 * keep being picked up by smallrye's normal annotation scan and stay visible in
 * /api/openapi and Swagger UI.
 */
@Path("/api")
public class LocaleTransferResource {

    private final LocaleRepository localeRepo;
    private final ImporterFactory  importerFactory;
    private final ExporterFactory  exporterFactory;

    @Inject
    public LocaleTransferResource(LocaleRepository localeRepo, ImporterFactory importerFactory,
                                  ExporterFactory exporterFactory) {
        this.localeRepo      = localeRepo;
        this.importerFactory = importerFactory;
        this.exporterFactory = exporterFactory;
    }

    @POST
    @Path("/locale/{localeId}/import/{fileType}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
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
