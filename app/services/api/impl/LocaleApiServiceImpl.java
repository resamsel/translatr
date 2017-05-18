package services.api.impl;

import java.io.File;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import criterias.LocaleCriteria;
import dto.NotFoundException;
import exporters.Exporter;
import exporters.GettextExporter;
import exporters.JavaPropertiesExporter;
import exporters.PlayMessagesExporter;
import forms.ImportLocaleForm;
import importers.GettextImporter;
import importers.Importer;
import importers.JavaPropertiesImporter;
import importers.PlayMessagesImporter;
import models.FileType;
import models.Locale;
import models.Scope;
import play.data.FormFactory;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Http.Context;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import services.LocaleService;
import services.ProjectService;
import services.api.LocaleApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class LocaleApiServiceImpl extends
    AbstractApiService<Locale, UUID, LocaleCriteria, dto.Locale> implements LocaleApiService {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleApiServiceImpl.class);

  private final ProjectService projectService;
  private final Injector injector;

  /**
   * @param localeService
   */
  @Inject
  protected LocaleApiServiceImpl(LocaleService localeService, ProjectService projectService,
      Injector injector) {
    super(localeService, dto.Locale.class, dto.Locale::from,
        new Scope[] {Scope.ProjectRead, Scope.LocaleRead},
        new Scope[] {Scope.ProjectRead, Scope.LocaleWrite});
    this.projectService = projectService;
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public dto.Locale upload(UUID localeId, Request request) {
    checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
        Scope.MessageWrite);

    Locale locale = service.byId(localeId);

    MultipartFormData<File> body = request.body().asMultipartFormData();
    if (body == null)
      throw new IllegalArgumentException(
          Context.current().messages().at("import.error.multipartMissing"));

    FilePart<File> messages = body.getFile("messages");

    if (messages == null)
      throw new IllegalArgumentException("Part 'messages' missing");

    ImportLocaleForm form =
        injector.instanceOf(FormFactory.class).form(ImportLocaleForm.class).bindFromRequest().get();

    LOGGER.debug("Type: {}", form.getFileType());

    Importer importer;
    switch (FileType.fromKey(form.getFileType())) {
      case PlayMessages:
        importer = injector.instanceOf(PlayMessagesImporter.class);
        break;
      case JavaProperties:
        importer = injector.instanceOf(JavaPropertiesImporter.class);
        break;
      case Gettext:
        importer = injector.instanceOf(GettextImporter.class);
        break;
      default:
        throw new IllegalArgumentException(
            "File type " + form.getFileType() + " not supported yet");
    }

    try {
      importer.apply(messages.getFile(), locale);
    } catch (Exception e) {
      LOGGER.error("Error while importing messages", e);
    }

    LOGGER.debug("End of import");

    return dtoMapper.apply(locale);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] download(UUID localeId, String fileType, Response response) {
    checkPermissionAll("Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
        Scope.MessageRead);

    Locale locale = service.byId(localeId);
    if (locale == null)
      throw new NotFoundException(dto.Locale.class.getSimpleName(), localeId);

    Exporter exporter;
    switch (FileType.fromKey(fileType)) {
      case PlayMessages:
        exporter = new PlayMessagesExporter();
        break;
      case JavaProperties:
        exporter = new JavaPropertiesExporter();
        break;
      case Gettext:
        exporter = new GettextExporter();
        break;
      default:
        throw new ValidationException("File type " + fileType + " not supported yet");
    }

    exporter.addHeaders(response, locale);

    return exporter.apply(locale);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Locale toModel(JsonNode json) {
    dto.Locale dto = Json.fromJson(json, dto.Locale.class);

    return dto.toModel(projectService.byId(dto.projectId));
  }
}
