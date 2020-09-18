package services.api.impl;

import criterias.LocaleCriteria;
import dto.NotFoundException;
import exporters.Exporter;
import forms.ImportLocaleForm;
import importers.Importer;
import mappers.LocaleMapper;
import models.FileType;
import models.Locale;
import models.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.FormFactory;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import repositories.LocaleRepository;
import services.LocaleService;
import services.PermissionService;
import services.ProjectService;
import services.api.LocaleApiService;
import utils.FileFormatRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.io.File;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class LocaleApiServiceImpl extends
    AbstractApiService<Locale, UUID, LocaleCriteria, LocaleService, dto.Locale> implements
    LocaleApiService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocaleApiServiceImpl.class);

  private final ProjectService projectService;
  private final Injector injector;

  @Inject
  protected LocaleApiServiceImpl(
      LocaleService localeService, ProjectService projectService,
      Injector injector, PermissionService permissionService, Validator validator) {
    super(localeService, dto.Locale.class, LocaleMapper::toDto,
        new Scope[]{Scope.ProjectRead, Scope.LocaleRead},
        new Scope[]{Scope.ProjectRead, Scope.LocaleWrite},
        permissionService,
        validator);

    this.projectService = projectService;
    this.injector = injector;
  }

  @Override
  public dto.Locale byOwnerAndProjectAndName(Http.Request request, String username, String projectName, String localeName,
                                             String... fetches) {
    permissionService
        .checkPermissionAll(request, "Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
            Scope.MessageRead);

    Locale locale = service.byOwnerAndProjectAndName(username, projectName, localeName, fetches);
    if (locale == null) {
      throw new NotFoundException(dto.Locale.class.getSimpleName(), localeName);
    }

    return dtoMapper.apply(locale);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public dto.Locale upload(UUID localeId, Request request) {
    permissionService
        .checkPermissionAll(request, "Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
            Scope.MessageWrite);

    Locale locale = ofNullable(service.byId(localeId))
        .orElseThrow(() -> new NotFoundException(dto.Locale.class.getSimpleName(), localeId));

    MultipartFormData<File> body = ofNullable(request.body().<File>asMultipartFormData())
        .orElseThrow(() -> new IllegalArgumentException(
            Context.current().messages().at("import.error.multipartMissing")));

    FilePart<File> messages = ofNullable(body.getFile("messages"))
        .orElseThrow(() -> new IllegalArgumentException("Part 'messages' missing"));

    ImportLocaleForm form = injector.instanceOf(FormFactory.class)
        .form(ImportLocaleForm.class)
        .bindFromRequest()
        .get();

    LOGGER.debug("Type: {}", form.getFileType());

    Importer importer = ofNullable(FileFormatRegistry.IMPORTER_MAP.get(FileType.fromKey(form.getFileType())))
        .map(injector::instanceOf)
        .orElseThrow(() -> new IllegalArgumentException("File type " + form.getFileType() + " not supported yet"));

    try {
      importer.apply(messages.getRef(), locale);
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
  public byte[] download(Http.Request request, UUID localeId, String fileType, Response response) {
    permissionService
        .checkPermissionAll(request, "Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
            Scope.MessageRead);

    Locale locale = ofNullable(service.byId(localeId, LocaleRepository.FETCH_MESSAGES))
        .orElseThrow(() -> new NotFoundException(dto.Locale.class.getSimpleName(), localeId));

    Exporter exporter = ofNullable(FileFormatRegistry.EXPORTER_MAP.get(FileType.fromKey(fileType)))
        .map(Supplier::get)
        .orElseThrow(() -> new ValidationException("File type " + fileType + " not supported yet"));
    exporter.addHeaders(response, locale);

    return exporter.apply(locale);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Locale toModel(dto.Locale in) {
    return LocaleMapper.toModel(in, projectService.byId(in.projectId));
  }
}
