package services.api.impl;

import criterias.GetCriteria;
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
import play.libs.Files;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.Request;
import play.mvc.Result;
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static play.mvc.Results.ok;

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
          Injector injector, PermissionService permissionService, Validator validator, LocaleMapper localeMapper) {
    super(localeService, dto.Locale.class, localeMapper::toDto,
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

    Locale locale = service.byOwnerAndProjectAndName(username, projectName, localeName, request, fetches);
    if (locale == null) {
      throw new NotFoundException(dto.Locale.class.getSimpleName(), localeName);
    }

    return dtoMapper.apply(locale, request);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public dto.Locale upload(UUID localeId, Request request) {
    permissionService
            .checkPermissionAll(request, "Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
                    Scope.MessageWrite);

    Locale locale = ofNullable(service.byId(GetCriteria.from(localeId, request)))
            .orElseThrow(() -> new NotFoundException(dto.Locale.class.getSimpleName(), localeId));

    MultipartFormData<Files.TemporaryFile> body = ofNullable(request.body().<Files.TemporaryFile>asMultipartFormData())
            .orElseThrow(() -> new IllegalArgumentException("import.error.multipartMissing"));

    FilePart<Files.TemporaryFile> messages = ofNullable(body.getFile("messages"))
            .orElseThrow(() -> new IllegalArgumentException("Part 'messages' missing"));

    ImportLocaleForm form = injector.instanceOf(FormFactory.class)
            .form(ImportLocaleForm.class)
            .bindFromRequest(request)
            .get();

    LOGGER.debug("Type: {}", form.getFileType());

    Importer importer = ofNullable(FileFormatRegistry.IMPORTER_MAP.get(FileType.fromKey(form.getFileType())))
            .map(injector::instanceOf)
            .orElseThrow(() -> new IllegalArgumentException("File type " + form.getFileType() + " not supported yet"));

    try {
      File tmpFile = File.createTempFile("translatr", "upload");
      messages.getRef().copyTo(tmpFile, true);
      importer.apply(tmpFile, locale, request);
      //noinspection ResultOfMethodCallIgnored
      tmpFile.delete();
    } catch (IOException e) {
      throw new RuntimeException("Error while creating temp file", e);
    } catch (Exception e) {
      throw new RuntimeException("Error while importing messages", e);
    }

    LOGGER.debug("End of import");

    return dtoMapper.apply(locale, request);
  }

  @Override
  public Result download(Request request, UUID localeId, String fileType) {
    permissionService
            .checkPermissionAll(request, "Access token not allowed", Scope.ProjectRead, Scope.LocaleRead,
                    Scope.MessageRead);

    Locale locale = ofNullable(service.byId(GetCriteria.from(localeId, request, LocaleRepository.FETCH_MESSAGES)))
            .orElseThrow(() -> new NotFoundException(dto.Locale.class.getSimpleName(), localeId));

    Exporter exporter = ofNullable(FileFormatRegistry.EXPORTER_MAP.get(FileType.fromKey(fileType)))
            .map(Supplier::get)
            .orElseThrow(() -> new ValidationException("File type " + fileType + " not supported yet"));

    return exporter.addHeaders(ok(new ByteArrayInputStream(exporter.apply(locale))), locale);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Locale toModel(dto.Locale in, Request request) {
    return LocaleMapper.toModel(in, projectService.byId(GetCriteria.from(in.projectId, request)));
  }
}
