package dto;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.AbstractController;
import controllers.routes;
import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;

import java.util.List;
import java.util.UUID;

import models.User;
import org.joda.time.DateTime;
import play.mvc.Call;
import services.KeyService;
import services.LocaleService;
import services.MessageService;

public class Project extends Dto {

  private static final long serialVersionUID = 4241999533661305290L;

  public UUID id;

  public DateTime whenCreated;

  public DateTime whenUpdated;

  public String name;

  public UUID ownerId;

  public String ownerName;

  public String ownerUsername;

  public List<Key> keys;

  public List<Locale> locales;

  public List<Message> messages;

  public List<ProjectUser> members;

  public Project() {
  }

  private Project(models.Project in) {
    this.id = in.id;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
    this.name = in.name;
    if (in.owner != null) {
      this.ownerId = in.owner.id;
      this.ownerName = in.owner.name;
      this.ownerUsername = in.owner.username;
    }

    if (in.keys != null && !in.keys.isEmpty()) {
      this.keys = in.keys.stream().map(Key::from).collect(toList());
    }

    if (in.locales != null && !in.locales.isEmpty()) {
      this.locales = in.locales.stream().map(Locale::from).collect(toList());
    }

    if (in.members != null && !in.members.isEmpty()) {
        this.members = in.members.stream().map(ProjectUser::from).collect(toList());
    }
  }

  public Project load(LocaleService localeService, KeyService keyService,
      MessageService messageService) {
    keys = keyService.findBy(new KeyCriteria().withProjectId(id)).getList().stream()
        .map(Key::from).collect(toList());
    locales = localeService.findBy(new LocaleCriteria().withProjectId(id)).getList().stream()
        .map(Locale::from).collect(toList());
    messages = messageService.findBy(new MessageCriteria().withProjectId(id)).getList().stream()
        .map(Message::from).collect(toList());

    return this;
  }

  public models.Project toModel() {
    models.Project out = new models.Project();

    out.id = id;
    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;
    out.name = name;
    if (ownerId != null) {
      out.owner = new User().withId(ownerId);
    } else {
      out.owner = User.loggedInUser();
    }

    return out;
  }

  /**
   * Return the route to the given key, with default params added.
   */
  public Call route() {
    return route(AbstractController.DEFAULT_SEARCH, AbstractController.DEFAULT_ORDER,
        AbstractController.DEFAULT_LIMIT, AbstractController.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the given key, with params added.
   */
  public Call route(String search, String order, int limit, int offset) {
    return routes.Projects.projectBy(ownerUsername, name);
  }

  public static Project from(models.Project project) {
    return new Project(project);
  }
}
