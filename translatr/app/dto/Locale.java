package dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.Project;

public class Locale extends Dto {
  public UUID id;

  @JsonIgnore
  public DateTime whenCreated;

  @JsonIgnore
  public DateTime whenUpdated;

  public UUID projectId;

  public String projectName;

  public String name;

  @JsonIgnore
  public List<Message> messages;

  public Locale() {}

  private Locale(models.Locale in) {
    this.id = in.id;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
    this.projectId = in.project.id;
    this.projectName = in.project.name;
    this.name = in.name;
    this.messages = in.messages.stream().map(m -> Message.from(m)).collect(Collectors.toList());
  }

  public models.Locale toModel() {
    return toModel(Project.byId(projectId));
  }

  public models.Locale toModel(Project project) {
    models.Locale out = new models.Locale();

    out.id = id;
    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;
    out.project = project;
    out.name = name;

    return out;
  }

  /**
   * @param locale
   * @return
   */
  public static Locale from(models.Locale locale) {
    return new Locale(locale);
  }
}
