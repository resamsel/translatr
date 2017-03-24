package dto;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import models.Project;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Key extends Dto {
  public UUID id;

  @JsonIgnore
  public DateTime whenCreated;

  @JsonIgnore
  public DateTime whenUpdated;

  public UUID projectId;

  public String projectName;

  public String name;

  public Map<String, Message> messages;

  public Key() {}

  private Key(models.Key in) {
    this.id = in.id;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
    this.projectId = in.project.id;
    this.projectName = in.project.name;
    this.name = in.name;

    if (!in.messages.isEmpty()) {
      this.messages =
          in.messages.stream().map(Message::from).collect(toMap(m -> m.localeName, m -> m));
    }
  }

  public models.Key toModel(Project project) {
    models.Key out = new models.Key();

    out.id = id;
    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;
    out.project = project;
    out.name = name;

    return out;
  }

  public static Key from(models.Key key) {
    return new Key(key);
  }
}
