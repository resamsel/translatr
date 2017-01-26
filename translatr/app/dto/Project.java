package dto;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import models.User;

public class Project extends Dto {
  public UUID id;

  @JsonIgnore
  public DateTime whenCreated;

  @JsonIgnore
  public DateTime whenUpdated;

  public String name;

  public UUID ownerId;

  public String ownerName;

  @JsonIgnore
  public List<Key> keys;

  @JsonIgnore
  public List<Locale> locales;

  @JsonIgnore
  public List<Message> messages;

  public Project() {}

  private Project(models.Project in) {
    this.id = in.id;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
    this.name = in.name;
    this.ownerId = in.owner.id;
    this.ownerName = in.owner.name;
  }

  public Project load() {
    keys = models.Key.findBy(new KeyCriteria().withProjectId(id)).stream().map(k -> Key.from(k))
        .collect(toList());
    locales = models.Locale.findBy(new LocaleCriteria().withProjectId(id)).stream()
        .map(l -> Locale.from(l)).collect(toList());
    messages = models.Message.findBy(new MessageCriteria().withProjectId(id)).stream()
        .map(m -> Message.from(m)).collect(toList());

    return this;
  }

  public models.Project toModel() {
    models.Project out = new models.Project();

    out.id = id;
    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;
    out.name = name;
    if (ownerId != null)
      out.owner = new User().withId(ownerId);
    else
      out.owner = User.loggedInUser();

    return out;
  }

  /**
   * @param project
   * @return
   */
  public static Project from(models.Project project) {
    return new Project(project);
  }
}
