package models;

import static utils.FormatUtils.formatLocale;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import controllers.AbstractController;
import controllers.Locales;
import controllers.routes;
import java.util.Arrays;
import java.util.Collections;
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
import play.api.mvc.Call;
import play.libs.Json;
import play.mvc.Http.Context;
import utils.UrlUtils;
import validators.LocaleNameUniqueChecker;
import validators.NameUnique;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "name"})})
@NameUnique(checker = LocaleNameUniqueChecker.class)
public class Locale implements Model<Locale, UUID>, Suggestable {

  public static final int NAME_LENGTH = 15;

  public static final String FETCH_MESSAGES = "messages";

  public static final List<String> PROPERTIES_TO_FETCH = Collections.singletonList("project");

  public static final Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of("project", Arrays.asList("project", "project.owner"), FETCH_MESSAGES,
          Arrays.asList(FETCH_MESSAGES, FETCH_MESSAGES + ".key"));

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

  public Locale() {
  }

  public Locale(Project project, String name) {
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

  @Override
  public String value() {
    Context ctx = Context.current();
    return ctx.messages().at("locale.autocomplete", formatLocale(ctx.lang().locale(), this));
  }

  @Override
  public Data data() {
    return Data.from(Locale.class, id, formatLocale(Context.current().lang().locale(), this),
        routes.Locales
            .localeBy(project.owner.username, project.name, name, Locales.DEFAULT_SEARCH,
                Locales.DEFAULT_ORDER, Locales.DEFAULT_LIMIT, Locales.DEFAULT_OFFSET)
            .absoluteURL(Context.current().request()));
  }

  @Override
  public Locale updateFrom(Locale in) {
    project = in.project;
    name = in.name;

    return this;
  }

  public static String getCacheKey(UUID localeId, String... fetches) {
    if (localeId == null) {
      return null;
    }

    if (fetches.length > 0) {
      return String.format("locale:%s:%s", localeId, StringUtils.join(fetches, ":"));
    }

    return String.format("locale:%s", localeId);
  }

  @Override
  public String toString() {
    return String.format("{\"project\": %s, \"name\": %s}", project, Json.toJson(name));
  }

  /**
   *
   */
  public String getPathName() {
    return UrlUtils.encode(name);
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
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");
    return routes.Locales
        .localeBy(Objects.requireNonNull(project.owner.username, "Project owner username is null"),
            Objects.requireNonNull(project.name, "Project name is null"),
            Objects.requireNonNull(name, "Name is null"), search, order, limit,
            offset);
  }

  public Call editRoute() {
    return editRoute(Locales.DEFAULT_SEARCH, Locales.DEFAULT_ORDER, Locales.DEFAULT_LIMIT,
        Locales.DEFAULT_OFFSET);
  }

  public Call editRoute(String search, String order, int limit, int offset) {
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");
    return routes.Locales
        .editBy(Objects.requireNonNull(project.owner.username, "Project owner username is null"),
            Objects.requireNonNull(project.name, "Project name is null"),
            Objects.requireNonNull(name, "Name is null"), search, order, limit,
            offset);
  }

  public Call doEditRoute() {
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");
    return routes.Locales
        .doEditBy(Objects.requireNonNull(project.owner.username, "Project owner username is null"),
            Objects.requireNonNull(project.name, "Project name is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  public Call removeRoute() {
    return removeRoute(Locales.DEFAULT_SEARCH, Locales.DEFAULT_ORDER, Locales.DEFAULT_LIMIT,
        Locales.DEFAULT_OFFSET);
  }

  public Call removeRoute(String search, String order, int limit, int offset) {
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");
    return routes.Locales
        .removeBy(Objects.requireNonNull(project.owner.username, "Project owner username is null"),
            Objects.requireNonNull(project.name, "Project name is null"),
            Objects.requireNonNull(name, "Name is null"), search, order, limit,
            offset);
  }

  public Call uploadRoute() {
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");
    return routes.Locales
        .uploadBy(Objects.requireNonNull(project.owner.username, "Project owner username is null"),
            Objects.requireNonNull(project.name, "Project name is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  public Call doUploadRoute() {
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");
    return routes.Locales.doUploadBy(
        Objects.requireNonNull(project.owner.username, "Project owner username is null"),
        Objects.requireNonNull(project.name, "Project name is null"),
        Objects.requireNonNull(name, "Name is null"));
  }
}
