package mappers;

import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import dto.Key;
import dto.Locale;
import dto.Project;
import models.User;
import services.KeyService;
import services.LocaleService;
import services.MessageService;

import static java.util.stream.Collectors.toList;

public class ProjectMapper {
  public static models.Project toModel(Project in) {
    models.Project out = new models.Project();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.name = in.name;
    out.description = in.description;
    if (in.ownerId != null) {
      out.owner = new User().withId(in.ownerId);
    } else {
      out.owner = User.loggedInUser();
    }

    return out;
  }

  public static models.Project toModel(Locale in) {
    if (in == null) {
      return null;
    }

    models.Project out = new models.Project();

    out.id = in.projectId;
    out.name = in.projectName;
    out.owner = new User();
    out.owner.username = in.projectOwnerUsername;

    return out;
  }

  public static models.Project toModel(Key in) {
    if (in == null) {
      return null;
    }

    models.Project out = new models.Project();

    out.id = in.projectId;
    out.name = in.projectName;
    out.owner = new User();
    out.owner.username = in.projectOwnerUsername;

    return out;
  }

  public static Project toDto(models.Project in) {
    Project out = new Project();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.name = in.name;
    out.description = in.description;
    if (in.owner != null) {
      out.ownerId = in.owner.id;
      out.ownerName = in.owner.name;
      out.ownerUsername = in.owner.username;
      out.ownerEmail = in.owner.email;
    }

    if (in.keys != null && !in.keys.isEmpty()) {
      out.keys = in.keys.stream().map(KeyMapper::toDto).collect(toList());
    }

    if (in.locales != null && !in.locales.isEmpty()) {
      out.locales = in.locales.stream().map(LocaleMapper::toDto).collect(toList());
    }

    if (in.members != null && !in.members.isEmpty()) {
      out.members = in.members.stream().map(ProjectUserMapper::toDto).collect(toList());
    }

    return out;
  }

  public static Project loadInto(models.Project in, LocaleService localeService, KeyService keyService, MessageService messageService) {
    Project out = toDto(in);

    out.keys = keyService.findBy(new KeyCriteria().withProjectId(in.id))
        .getList()
        .stream()
        .map(KeyMapper::toDto)
        .collect(toList());
    out.locales = localeService.findBy(new LocaleCriteria().withProjectId(in.id))
        .getList()
        .stream()
        .map(LocaleMapper::toDto)
        .collect(toList());
    out.messages = messageService.findBy(new MessageCriteria().withProjectId(in.id))
        .getList()
        .stream()
        .map(MessageMapper::toDto)
        .collect(toList());

    return out;
  }
}
