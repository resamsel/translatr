package dto;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import controllers.AbstractController;
import controllers.routes;
import models.Project;
import play.api.mvc.Call;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Key extends Dto {
  private static final long serialVersionUID = 4515423450076475406L;

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

  public Key() {}

  private Key(models.Key in) {
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
          in.messages.stream().map(Message::from).collect(toMap(m -> m.localeName, m -> m));
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

  /**
   * Return the route to the given key, with default params added.
   * 
   * @return
   */
  public Call route() {
    return route(AbstractController.DEFAULT_SEARCH, AbstractController.DEFAULT_ORDER,
        AbstractController.DEFAULT_LIMIT, AbstractController.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the given key, with params added.
   * 
   * @param search
   * @param order
   * @param limit
   * @param offset
   * @return
   */
  public Call route(String search, String order, int limit, int offset) {
    if(projectOwnerUsername == null || projectName == null || pathName == null)
      return null;

    return routes.Keys.keyBy(projectOwnerUsername, projectName, pathName, search, order, limit,
        offset);
  }

  public static Key from(models.Key key) {
    return new Key(key);
  }
}
