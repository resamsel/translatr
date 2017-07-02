package models;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;

import controllers.routes;
import criterias.HasNextPagedList;
import criterias.MessageCriteria;
import criterias.ProjectCriteria;
import play.api.Play;
import play.data.validation.Constraints.Required;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Http.Context;
import services.MessageService;
import services.ProjectService;
import utils.ContextKey;
import utils.PermissionUtils;
import utils.QueryUtils;
import validators.NameUnique;
import validators.ProjectNameUniqueChecker;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"owner_id", "name"})})
@NameUnique(checker = ProjectNameUniqueChecker.class)
public class Project implements Model<Project, UUID>, Suggestable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Project.class);

  public static final int NAME_LENGTH = 255;

  public static final String FETCH_MEMBERS = "members";
  public static final String FETCH_OWNER = "owner";
  public static final String FETCH_LOCALES = "locales";
  public static final String FETCH_KEYS = "keys";

  private static final List<String> PROPERTIES_TO_FETCH = Arrays.asList(FETCH_OWNER, FETCH_MEMBERS);

  private static final Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of("project", Arrays.asList("project"), FETCH_MEMBERS,
          Arrays.asList(FETCH_MEMBERS, FETCH_MEMBERS + ".user"));

  private static final Find<UUID, Project> find = new Find<UUID, Project>() {};

  @Id
  @GeneratedValue
  public UUID id;

  @Version
  public Long version;

  public boolean deleted;

  @JsonIgnore
  @CreatedTimestamp
  public DateTime whenCreated;

  @JsonIgnore
  @UpdatedTimestamp
  public DateTime whenUpdated;

  @Column(nullable = false, length = NAME_LENGTH)
  @Required
  public String name;

  @Column(nullable = false, length = NAME_LENGTH)
  @Required
  public String path;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  @Required
  public User owner;

  public Integer wordCount;

  @JsonIgnore
  @OneToMany
  public List<Locale> locales;

  @JsonIgnore
  @OneToMany
  public List<Key> keys;

  @JsonIgnore
  @OneToMany(cascade = CascadeType.PERSIST)
  public List<ProjectUser> members;

  @Transient
  private Long localesSize;

  @Transient
  private Long keysSize;

  @Transient
  private List<Message> messages;

  @Transient
  private Map<UUID, Long> localesSizeMap;

  @Transient
  private Map<UUID, Long> keysSizeMap;

  public Project() {}

  public Project(String name) {
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
    return name;
  }

  @Override
  public Data data() {
    return Data.from(Project.class, id, name, route().absoluteURL(Context.current().request()));
  }

  public Project withId(UUID id) {
    this.id = id;
    return this;
  }

  public static Project byId(UUID id, String... fetches) {
    if (id == null)
      return null;

    return QueryUtils
        .fetch(find.setId(id), QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP)
        .findUnique();
  }

  public static PagedList<Project> findBy(ProjectCriteria criteria) {
    ExpressionList<Project> query =
        find.fetch(FETCH_OWNER).fetch(FETCH_MEMBERS).fetch(FETCH_LOCALES).fetch(FETCH_KEYS).where();

    query.eq("deleted", false);

    if (criteria.getOwnerId() != null)
      query.eq("owner.id", criteria.getOwnerId());

    if (criteria.getMemberId() != null)
      query.eq("members.user.id", criteria.getMemberId());

    if (criteria.getProjectId() != null)
      query.idEq(criteria.getProjectId());

    if (criteria.getSearch() != null)
      query.ilike("name", "%" + criteria.getSearch() + "%");

    criteria.paged(query);

    return log(() -> HasNextPagedList.create(query), LOGGER, "findBy");
  }

  public float progress() {
    long keysSize = keysSize();
    long localesSize = localesSize();
    if (keysSize < 1 || localesSize < 1)
      return 0f;
    return (float) Message.countBy(this) / (float) (keysSize * localesSize);
  }

  public long missingKeys(UUID localeId) {
    return keysSize() - keysSizeMap(localeId);
  }

  public long keysSizeMap(UUID localeId) {
    if (keysSizeMap == null)
      // FIXME: This is an expensive operation, consider doing this in a group by query.
      keysSizeMap = messageList().stream().collect(groupingBy(m -> m.locale.id, counting()));

    return keysSizeMap.getOrDefault(localeId, 0l);
  }

  public long missingLocales(UUID keyId) {
    return localesSize() - localesSizeMap(keyId);
  }

  public long localesSizeMap(UUID keyId) {
    if (localesSizeMap == null)
      // FIXME: This is an expensive operation, consider doing this in a group by query.
      localesSizeMap = messageList().stream().collect(groupingBy(m -> m.key.id, counting()));

    return localesSizeMap.getOrDefault(keyId, 0l);
  }

  public List<Message> messageList() {
    if (messages == null)
      messages = Message.findBy(new MessageCriteria().withProjectId(this.id)).getList();

    return messages;
  }

  public long membersSize() {
    return members.size();
  }

  public long localesSize() {
    return locales.size();
  }

  public long keysSize() {
    return keys.size();
  }

  public long messagesSize() {
    return Play.current().injector().instanceOf(MessageService.class).countBy(this);
  }

  public Project withName(String name) {
    this.name = name;
    return this;
  }

  public Project withPath(String path) {
    this.path = path;
    return this;
  }

  public Project withOwner(User owner) {
    this.owner = owner;
    return this;
  }

  public Project withDeleted(boolean deleted) {
    this.deleted = deleted;
    return this;
  }

  /**
   * @param name
   * @return
   */
  public static Project byOwnerAndName(User user, String name, String... fetches) {
    return QueryUtils
        .fetch(find.query(), QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP)
        .where().eq("owner", user).eq("name", name).findUnique();
  }

  /**
   * @param path
   * @return
   */
  public static Project byOwnerAndPath(User user, String path, String... fetches) {
    return QueryUtils
        .fetch(find.query(), QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP)
        .where().eq("owner", user).eq("path", path).findUnique();
  }

  /**
   * @param project
   */
  @Override
  public Project updateFrom(Project in) {
    name = in.name;
    owner = in.owner;

    return this;
  }

  public boolean hasRolesAny(User user, ProjectRole... roles) {
    Map<UUID, List<ProjectRole>> userMap =
        members.stream().collect(groupingBy(m -> m.user.id, mapping(m -> m.role, toList())));
    if (!userMap.containsKey(user.id))
      return false;

    List<ProjectRole> userRoles = userMap.get(user.id);
    userRoles.retainAll(Arrays.asList(roles));

    return !userRoles.isEmpty();
  }

  public ProjectRole roleOf(User user) {
    if (user == null)
      return null;

    if (user.equals(owner))
      return ProjectRole.Owner;

    Optional<ProjectUser> member = members.stream().filter(m -> user.equals(m.user)).findFirst();
    if (member.isPresent())
      return member.get().role;

    return null;
  }

  public static UUID brandProjectId() {
    UUID brandProjectId = ContextKey.BrandProjectId.get();
    if (brandProjectId != null)
      return brandProjectId;

    User user = User.loggedInUser();
    if (user == null)
      return null;

    Project brandProject = null;
    try {
      brandProject = Play.current().injector().instanceOf(ProjectService.class).byOwnerAndName(user,
          "Translatr");
    } catch (Exception e) {
      LOGGER.warn("Error while retrieving brand project", e);
    }

    if (brandProject == null)
      return null;

    ContextKey.BrandProjectId.put(Context.current(), brandProject.id);

    return brandProject.id;
  }

  /**
   * @param user
   * @param role
   * @return
   */
  public boolean hasPermissionAny(User user, ProjectRole... roles) {
    return PermissionUtils.hasPermissionAny(id, user, roles);
  }

  /**
   * @param projectId
   * @param fetches
   * @return
   */
  public static String getCacheKey(UUID projectId, String... fetches) {
    if (projectId == null)
      return null;

    if (fetches.length > 0)
      return String.format("project:%s:%s", projectId, StringUtils.join(fetches, ":"));

    return String.format("project:%s", projectId);
  }

  public static String getCacheKey(User user, String projectName, String... fetches) {
    if (user == null || StringUtils.isEmpty(projectName))
      return null;

    if (fetches.length > 0)
      return String.format("project:owner:%s:name:%s:%s", user.id, projectName,
          StringUtils.join(fetches, ":"));

    return String.format("project:owner:%s:name:%s", user.id, projectName);
  }

  /**
   * Return the route to the given project.
   * 
   * @param key
   * @return
   */
  public Call route() {
    return routes.Projects.projectBy(owner.username, path);
  }

  @Override
  public String toString() {
    return String.format("{\"name\": %s, \"owner\": %s}", Json.toJson(name), owner);
  }
}
