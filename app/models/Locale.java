package models;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.FormatUtils.formatLocale;
import static utils.Stopwatch.log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;

import controllers.AbstractController;
import controllers.Locales;
import controllers.routes;
import criterias.HasNextPagedList;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import play.api.mvc.Call;
import play.libs.Json;
import play.mvc.Http.Context;
import utils.QueryUtils;
import utils.UrlUtils;
import validators.LocaleNameUniqueChecker;
import validators.NameUnique;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "name"})})
@NameUnique(checker = LocaleNameUniqueChecker.class)
public class Locale implements Model<Locale, UUID>, Suggestable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Locale.class);

  public static final int NAME_LENGTH = 15;

  public static final String FETCH_MESSAGES = "messages";

  private static final Find<UUID, Locale> find = new Find<UUID, Locale>() {
  };

  private static final List<String> PROPERTIES_TO_FETCH = Arrays.asList("project");

  private static final Map<String, List<String>> FETCH_MAP =
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

  /**
   * @return
   */
  public static Locale byId(UUID id, String... fetches) {
    return QueryUtils.fetch(find.setId(id).setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP).findUnique();
  }

  /**
   * @param project
   * @return
   */
  public static List<Locale> byProject(Project project) {
    return find.fetch("project").where().eq("project", project).findList();
  }

  /**
   * @param project
   * @param name
   * @return
   */
  public static Locale byProjectAndName(Project project, String name) {
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
  public static Locale byProjectAndName(UUID projectId, String name) {
    return find.fetch("project").where().eq("project.id", projectId).eq("name", name).findUnique();
  }

  /**
   * @param criteria
   * @return
   */
  public static PagedList<Locale> findBy(LocaleCriteria criteria) {
    Query<Locale> q = QueryUtils.fetch(find.query().alias("k").setDisableLazyLoading(true),
        PROPERTIES_TO_FETCH, FETCH_MAP);

    if (StringUtils.isEmpty(criteria.getMessagesKeyName()) && !criteria.getFetches().isEmpty()) {
      q = QueryUtils.fetch(q, QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, criteria.getFetches()),
          FETCH_MAP);
    }

    ExpressionList<Locale> query = q.where();

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getLocaleName() != null) {
      query.eq("name", criteria.getLocaleName());
    }

    if (criteria.getSearch() != null) {
      query.ilike("name", "%" + criteria.getSearch() + "%");
    }

    if (criteria.getOrder() != null) {
      query.setOrderBy(criteria.getOrder());
    }

    criteria.paged(query);

    return log(() -> fetch(HasNextPagedList.create(query), criteria), LOGGER, "findBy");
  }

  private static HasNextPagedList<Locale> fetch(HasNextPagedList<Locale> paged,
      LocaleCriteria criteria) {
    if (StringUtils.isNotEmpty(criteria.getMessagesKeyName())
        && criteria.getFetches().contains("messages")) {
      // Retrieve messages that match the given keyName and locales retrieved
      Map<UUID, Message> messages = Message
          .findBy(new MessageCriteria().withKeyName(criteria.getMessagesKeyName())
              .withLocaleIds(paged.getList().stream().map(l -> l.id).collect(toList())))
          .getList().stream().collect(toMap(m -> m.locale.id, m -> m));

      for (Locale locale : paged.getList()) {
        if (messages.containsKey(locale.id)) {
          locale.messages = Arrays.asList(messages.get(locale.id));
        }
      }
    }

    return paged;
  }

  public static List<Locale> last(Project project, int limit) {
    return log(() -> find.fetch("project").where().eq("project", project).order("whenUpdated desc")
        .setMaxRows(limit).findList(), LOGGER, "last(%d)", limit);
  }

  /**
   * @param in
   */
  @Override
  public Locale updateFrom(Locale in) {
    project = in.project;
    name = in.name;

    return this;
  }

  /**
   * @param localeId
   * @param fetches
   * @return
   */
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
    return routes.Locales.localeBy(project.owner.username, project.name, name, search, order, limit,
        offset);
  }

  /**
   * @return
   */
  public Call editRoute() {
    return editRoute(Locales.DEFAULT_SEARCH, Locales.DEFAULT_ORDER, Locales.DEFAULT_LIMIT,
        Locales.DEFAULT_OFFSET);
  }

  /**
   * @param search
   * @param order
   * @param limit
   * @param offset
   * @return
   */
  public Call editRoute(String search, String order, int limit, int offset) {
    return routes.Locales.editBy(project.owner.username, project.name, name, search, order, limit,
        offset);
  }

  /**
   * @return
   */
  public Call doEditRoute() {
    return routes.Locales.doEditBy(project.owner.username, project.name, name);
  }

  /**
   * @return
   */
  public Call removeRoute() {
    return removeRoute(Locales.DEFAULT_SEARCH, Locales.DEFAULT_ORDER, Locales.DEFAULT_LIMIT,
        Locales.DEFAULT_OFFSET);
  }

  /**
   * @param search
   * @param order
   * @param limit
   * @param offset
   * @return
   */
  public Call removeRoute(String search, String order, int limit, int offset) {
    return routes.Locales.removeBy(project.owner.username, project.name, name, search, order, limit,
        offset);
  }
}
