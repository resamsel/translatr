package models;

import static utils.Stopwatch.log;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import controllers.Keys;
import controllers.routes;
import criterias.HasNextPagedList;
import criterias.KeyCriteria;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.mvc.Call;
import play.libs.Json;
import play.mvc.Http.Context;
import utils.QueryUtils;
import utils.UrlUtils;
import validators.KeyNameUniqueChecker;
import validators.NameUnique;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "name"})})
@NameUnique(checker = KeyNameUniqueChecker.class)
public class Key implements Model<Key, UUID>, Suggestable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Key.class);

  public static final int NAME_LENGTH = 255;

  private static final Find<UUID, Key> find = new Find<UUID, Key>() {
  };

  private static final Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of("project", Arrays.asList("project", "project.owner"), "messages",
          Arrays.asList("messages", "messages.locale"));

  private static final List<String> PROPERTIES_TO_FETCH = Arrays.asList("project");

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

  @Column(nullable = false, length = NAME_LENGTH)
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
   * @return
   */
  public static Key byId(UUID id, String... fetches) {
    return QueryUtils.fetch(find.setId(id).setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP).findUnique();
  }

  /**
   * @param criteria
   * @return
   */
  public static PagedList<Key> findBy(KeyCriteria criteria) {
    Query<Key> q = fetch(criteria.getFetches());

    ExpressionList<Key> query = q.where();

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getSearch() != null) {
      query.disjunction().ilike("name", "%" + criteria.getSearch() + "%")
          .exists(Ebean.createQuery(Message.class).where().raw("key.id = k.id")
              .ilike("value", "%" + criteria.getSearch() + "%").query())
          .endJunction();
    }

    if (Boolean.TRUE.equals(criteria.getMissing())) {
      ExpressionList<Message> messageQuery =
          Ebean.createQuery(Message.class).where().raw("key.id = k.id");

      if (criteria.getLocaleId() != null) {
        messageQuery.eq("locale.id", criteria.getLocaleId());
      }

      query.notExists(messageQuery.query());
    }

    if (criteria.getOrder() != null) {
      query.setOrderBy(criteria.getOrder());
    }

    criteria.paged(query);

    return log(() -> HasNextPagedList.create(query), LOGGER, "findBy");
  }

  private static Query<Key> fetch(List<String> fetches) {
    return fetch(fetches.toArray(new String[fetches.size()]));
  }

  private static Query<Key> fetch(String... fetches) {
    return QueryUtils.fetch(find.query().alias("k").setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP);
  }

  /**
   * @param project
   * @param name
   * @return
   */
  public static Key byProjectAndName(Project project, String name) {
    if (project == null) {
      return null;
    }

    return byProjectAndName(project.id, name);
  }

  /**
   * @param projectId
   * @param name
   * @return
   */
  public static Key byProjectAndName(UUID projectId, String name) {
    return fetch().where().eq("project.id", projectId).eq("name", name).findUnique();
  }

  public static List<Key> last(Project project, int limit) {
    return fetch().where().eq("project", project).order("whenUpdated desc")
        .setMaxRows(limit).findList();
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

  /**
   * @param keyId
   * @param fetches
   * @return
   */
  public static String getCacheKey(UUID keyId, String... fetches) {
    if (keyId == null) {
      return null;
    }

    if (fetches.length > 0) {
      return String.format("key:%s:%s", keyId, StringUtils.join(fetches, ":"));
    }

    return String.format("key:%s", keyId);
  }

  @Override
  public String toString() {
    return String.format("{\"project\": %s, \"name\": %s}", project, Json.toJson(name));
  }

  /**
   * @return
   */
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
   * Return the route to the remove route.
   */
  public Call removeRoute() {
    return removeRoute(Keys.DEFAULT_SEARCH, Keys.DEFAULT_ORDER, Keys.DEFAULT_LIMIT,
        Keys.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the remove route with params.
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
