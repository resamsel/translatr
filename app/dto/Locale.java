package dto;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import controllers.AbstractController;
import controllers.routes;
import criterias.MessageCriteria;
import java.util.Map;
import java.util.UUID;
import models.Project;
import org.joda.time.DateTime;
import play.api.mvc.Call;

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
  public String projectOwnerUsername;

  public String name;

  public String pathName;

  public Map<String, Message> messages;

  public Locale() {}

  private Locale(models.Locale in) {
    this.id = in.id;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
    this.projectId = in.project.id;
    this.projectName = in.project.name;
    this.projectOwnerUsername = in.project.owner.username;
    this.name = in.name;
    this.pathName = in.getPathName();

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
   * Return the route to the given key, with default params added.
   * 
   * @param key
   * @return
   */
  public Call route() {
    return route(AbstractController.DEFAULT_SEARCH, AbstractController.DEFAULT_ORDER,
        AbstractController.DEFAULT_LIMIT, AbstractController.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the given key, with params added.
   * 
   * @param key
   * @param search
   * @param order
   * @param limit
   * @param offset
   * @return
   */
  public Call route(String search, String order, int limit, int offset) {
    if (projectOwnerUsername == null || projectName == null || pathName == null)
      return null;

    return routes.Locales.localeBy(projectOwnerUsername, projectName, pathName, search, order,
        limit, offset);
  }

  /**
   * @param locale
   * @return
   */
  public static Locale from(models.Locale locale) {
    return new Locale(locale);
  }
}
