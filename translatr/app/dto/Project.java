package dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    this.keys = in.keys.stream().map(k -> Key.from(k)).collect(Collectors.toList());
    this.locales = in.locales.stream().map(l -> Locale.from(l)).collect(Collectors.toList());
    this.messages = models.Message.findBy(new MessageCriteria().withProjectId(in.id)).stream()
        .map(m -> Message.from(m)).collect(Collectors.toList());
  }

  public models.Project toModel() {
    models.Project out = new models.Project();

    out.id = id;
    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;
    out.name = name;
    if (ownerId != null)
      out.owner = new User().withId(ownerId);

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
