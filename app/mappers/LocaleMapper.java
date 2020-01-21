package mappers;

import com.google.common.base.Strings;
import criterias.MessageCriteria;
import dto.Locale;
import dto.Message;
import models.Project;
import services.MessageService;
import utils.FormatUtils;

import static java.util.stream.Collectors.toMap;

public class LocaleMapper {
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

    return out;
  }

  public static Locale toDto(models.Locale in) {
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
    out.displayName = FormatUtils.formatDisplayName(in);
    if (Strings.isNullOrEmpty(out.displayName)) {
      out.displayName = in.name;
    }
    out.progress = in.progress;

    if (in.messages != null && !in.messages.isEmpty()) {
      out.messages =
          in.messages.stream().map(MessageMapper::toDto).collect(toMap(m -> m.keyName, m -> m));
    }

    return out;
  }

  public static Locale loadInto(models.Locale in, MessageService messageService) {
    Locale out = toDto(in);

    if (out.messages == null) {
      out.messages = messageService.findBy(new MessageCriteria().withLocaleId(in.id))
          .getList()
          .stream()
          .map(MessageMapper::toDto)
          .collect(toMap(m -> m.keyName, m -> m));
    }

    return out;
  }
}
