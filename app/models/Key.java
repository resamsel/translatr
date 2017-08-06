package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.Keys;
import controllers.routes;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import play.api.mvc.Call;
import play.libs.Json;
import play.mvc.Http.Context;
import utils.CacheUtils;
import utils.UrlUtils;
import validators.KeyNameUniqueChecker;
import validators.NameUnique;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "name"})})
@NameUnique(checker = KeyNameUniqueChecker.class)
public class Key implements Model<Key, UUID>, Suggestable {

  public static final int NAME_LENGTH = 255;

  @Id
  @GeneratedValue
  public UUID id;

  @Version
  public Long version;

  @CreatedTimestamp
  public DateTime whenCreated;

  @UpdatedTimestamp
  public DateTime whenUpdated;

  @ManyToOne(optional = false)
  @NotNull
  public Project project;

  @Column(nullable = false)
  @NotNull
  public String name;

  public Integer wordCount;

  @JsonIgnore
  @OneToMany
  public List<Message> messages;

  public Key() {
  }

  public Key(Project project, String name) {
    this.project = project;
    this.name = name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UUID getId() {
    return id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String value() {
    return Context.current().messages().at("key.autocomplete", name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Data data() {
    return Data.from(Key.class, id, name, route().absoluteURL(Context.current().request()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Key updateFrom(Key in) {
    project = in.project;
    name = in.name;

    return this;
  }

  public static String getCacheKey(UUID keyId, String... fetches) {
    return CacheUtils.getCacheKey("key:id", keyId, fetches);
  }

  @Override
  public String toString() {
    return String.format("{\"project\": %s, \"name\": %s}", project, Json.toJson(name));
  }

  public String getPathName() {
    return UrlUtils.encode(name);
  }

  /**
   * Return the route to the given key, with default params added.
   */
  public Call route() {
    return route(Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT, Keys.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the given key, with params added.
   */
  public Call route(String search, String order, int limit, int offset) {
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");
    return routes.Keys
        .keyBy(Objects.requireNonNull(project.owner.username, "Project owner username is null"),
            Objects.requireNonNull(project.name, "Project name is null"),
            Objects.requireNonNull(name, "Name is null"), search, order, limit,
            offset);
  }

  /**
   * Return the route to the edit route.
   */
  public Call editRoute() {
    return editRoute(Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT,
        Keys.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the edit route with params.
   */
  public Call editRoute(String search, String order, int limit, int offset) {
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");
    return routes.Keys
        .editBy(Objects.requireNonNull(project.owner.username, "Project owner username is null"),
            Objects.requireNonNull(project.name, "Project name is null"),
            Objects.requireNonNull(name, "Name is null"), search, order, limit, offset);
  }

  /**
   * Return the route to the do edit route.
   */
  public Call doEditRoute() {
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");
    return routes.Keys
        .doEditBy(Objects.requireNonNull(project.owner.username, "Project owner username is null"),
            Objects.requireNonNull(project.name, "Project name is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  /**
   * Return the route to the removeAll route.
   */
  public Call removeRoute() {
    return removeRoute(Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT,
        Keys.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the removeAll route with params.
   */
  public Call removeRoute(String search, String order, int limit, int offset) {
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");
    return routes.Keys
        .removeBy(Objects.requireNonNull(project.owner.username, "Project owner username is null"),
            Objects.requireNonNull(project.name, "Project name is null"),
            Objects.requireNonNull(name, "Name is null"), null, search, order, limit,
            offset);
  }
}
