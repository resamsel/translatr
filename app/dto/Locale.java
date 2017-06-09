package dto;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import criterias.MessageCriteria;
import models.Project;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Locale extends Dto {
  private static final long serialVersionUID = -2778174337389175121L;

  public UUID id;

  @JsonIgnore
  public DateTime whenCreated;

  @JsonIgnore
  public DateTime whenUpdated;

  public UUID projectId;

  public String projectName;

  public String name;

  public Map<String, Message> messages;

  public Locale() {}

  private Locale(models.Locale in) {
    this.id = in.id;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
    this.projectId = in.project.id;
    this.projectName = in.project.name;
    this.name = in.name;

    if (in.messages != null && !in.messages.isEmpty())
      this.messages =
          in.messages.stream().map(Message::from).collect(toMap(m -> m.keyName, m -> m));
  }

  public Locale load() {
    if (messages == null)
      messages = models.Message.findBy(new MessageCriteria().withLocaleId(id)).getList().stream()
          .map(Message::from).collect(toMap(m -> m.keyName, m -> m));

    return this;
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
