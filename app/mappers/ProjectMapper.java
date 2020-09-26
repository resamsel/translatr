package mappers;

import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import dto.Key;
import dto.Locale;
import dto.Project;
import models.User;
import play.mvc.Http;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import utils.EmailUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.stream.Collectors.toList;

@Singleton
public class ProjectMapper {
  private final LocaleMapper localeMapper;
  private final KeyMapper keyMapper;
  private final MessageMapper messageMapper;

  @Inject
  public ProjectMapper(LocaleMapper localeMapper, KeyMapper keyMapper, MessageMapper messageMapper) {
    this.localeMapper = localeMapper;
    this.keyMapper = keyMapper;
    this.messageMapper = messageMapper;
  }

  public static models.Project toModel(Project in) {
    models.Project out = new models.Project();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.name = in.name;
    out.description = in.description;
    if (in.ownerId != null) {
      out.owner = new User()
              .withId(in.ownerId)
              .withUsername(in.ownerUsername)
              .withName(in.ownerName);
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

  public Project toDto(models.Project in, Http.Request request) {
    Project out = new Project();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.name = in.name;
    out.description = in.description;
    out.wordCount = in.wordCount;
    out.progress = in.progress;
    out.myRole = ProjectRoleMapper.toDto(in.myRole);

    if (in.owner != null) {
      out.ownerId = in.owner.id;
      out.ownerName = in.owner.name;
      out.ownerUsername = in.owner.username;
      out.ownerEmailHash = EmailUtils.hashEmail(in.owner.email);
    }

    if (in.keys != null && !in.keys.isEmpty()) {
      out.keys = in.keys.stream()
              .map(key -> keyMapper.toDto(key, request))
              .collect(toList());
    }

    if (in.locales != null && !in.locales.isEmpty()) {
      out.locales = in.locales.stream()
              .map(locale -> localeMapper.toDto(locale, request))
              .collect(toList());
    }

    if (in.members != null && !in.members.isEmpty()) {
      out.members = in.members.stream().map(ProjectUserMapper::toDto).collect(toList());
    }

    return out;
  }

  public Project loadInto(models.Project in, LocaleService localeService, KeyService keyService, MessageService messageService, Http.Request request) {
    Project out = toDto(in, request);

    out.keys = keyService.findBy(new KeyCriteria().withProjectId(in.id))
            .getList()
            .stream()
            .map(key -> keyMapper.toDto(key, request))
            .collect(toList());
    out.locales = localeService.findBy(new LocaleCriteria().withProjectId(in.id))
            .getList()
            .stream()
            .map(locale -> localeMapper.toDto(locale, request))
            .collect(toList());
    out.messages = messageService.findBy(new MessageCriteria().withProjectId(in.id))
            .getList()
            .stream()
            .map(message -> messageMapper.toDto(message, request))
            .collect(toList());

    return out;
  }
}
