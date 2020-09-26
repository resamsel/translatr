package mappers;

import com.google.common.base.Strings;
import criterias.MessageCriteria;
import dto.Locale;
import dto.Message;
import models.Project;
import models.User;
import play.mvc.Http;
import services.MessageService;
import utils.FormatUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.stream.Collectors.toMap;

@Singleton
public class LocaleMapper {
  private final FormatUtils formatUtils;
  private final MessageMapper messageMapper;

  @Inject
  public LocaleMapper(FormatUtils formatUtils, MessageMapper messageMapper) {
    this.formatUtils = formatUtils;
    this.messageMapper = messageMapper;
  }

  public static models.Locale toModel(Locale in) {
    return toModel(in, ProjectMapper.toModel(in));
  }

  public static models.Locale toModel(Locale in, Project project) {
    models.Locale out = new models.Locale();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.project = project;
    out.name = in.name;

    return out;
  }

  public static models.Locale toModel(Message in) {
    if (in == null) {
      return null;
    }

    models.Locale out = new models.Locale();

    out.id = in.localeId;
    out.name = in.localeName;

    if (in.projectId != null) {
      out.project = new Project()
          .withId(in.projectId)
          .withName(in.projectName);

      if (in.projectOwnerUsername != null) {
        out.project.owner = new User().withUsername(in.projectOwnerUsername);
      }
    }

    return out;
  }

  public Locale toDto(models.Locale in, Http.Request request) {
    Locale out = new Locale();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;

    if (in.project != null) {
      out.projectId = in.project.id;
      out.projectName = in.project.name;
      out.projectOwnerUsername = in.project.owner.username;
    }

    out.name = in.name;
    out.pathName = in.getPathName();
    out.displayName = formatUtils.formatDisplayName(in, request);
    if (Strings.isNullOrEmpty(out.displayName)) {
      out.displayName = in.name;
    }
    out.progress = in.progress;

    if (in.messages != null && !in.messages.isEmpty()) {
      out.messages =
          in.messages.stream()
                  .map(message -> messageMapper.toDto(message, request))
                  .collect(toMap(m -> m.keyName, m -> m));
    }

    return out;
  }

  public Locale loadInto(models.Locale in, MessageService messageService, Http.Request request) {
    Locale out = toDto(in, request);

    if (out.messages == null) {
      out.messages = messageService.findBy(new MessageCriteria().withLocaleId(in.id))
          .getList()
          .stream()
          .map(message -> messageMapper.toDto(message, request))
          .collect(toMap(m -> m.keyName, m -> m));
    }

    return out;
  }
}
