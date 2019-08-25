package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.AbstractController;
import criterias.MessageCriteria;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Play;
import play.api.mvc.Call;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import play.libs.Json;
import play.mvc.Http.Context;
import services.MessageService;
import services.ProjectService;
import utils.CacheUtils;
import utils.ContextKey;
import validators.NameUnique;
import validators.ProjectName;
import validators.ProjectNameUniqueChecker;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"owner_id", "name"})})
@NameUnique(checker = ProjectNameUniqueChecker.class, message = "error.projectnameunique")
public class Project implements Model<Project, UUID>, Suggestable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Project.class);

  public static final int NAME_LENGTH = 255;

  @Id
  @GeneratedValue
  public UUID id;

  public boolean deleted;

  @JsonIgnore
  @CreatedTimestamp
  public DateTime whenCreated;

  @JsonIgnore
  @UpdatedTimestamp
  public DateTime whenUpdated;

  @Column(nullable = false)
  @ManyToOne
  @JoinColumn(name = "owner_id")
  @Required
  public User owner;

  @Column(nullable = false)
  @Required
  @Constraints.Pattern("[^\\s/]*")
  @ProjectName
  public String name;

  @Constraints.MaxLength(2000)
  public String description;

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
  private List<Message> messages;

  @Transient
  private Map<UUID, Long> localesSizeMap;

  @Transient
  private Map<UUID, Long> keysSizeMap;

  public Project() {
  }

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

  public float progress() {
    long keysSize = keysSize();
    long localesSize = localesSize();
    if (keysSize < 1 || localesSize < 1) {
      return 0f;
    }
    return (float) messagesSize() / (float) (keysSize * localesSize);
  }

  public long missingKeys(UUID localeId) {
    return keysSize() - keysSizeMap(localeId);
  }

  private long keysSizeMap(UUID localeId) {
    if (keysSizeMap == null)
    // FIXME: This is an expensive operation, consider doing this in a group by query.
    {
      keysSizeMap = messageList().stream().collect(groupingBy(m -> m.locale.id, counting()));
    }

    return keysSizeMap.getOrDefault(localeId, 0L);
  }

  public long missingLocales(UUID keyId) {
    return localesSize() - localesSizeMap(keyId);
  }

  private long localesSizeMap(UUID keyId) {
    if (localesSizeMap == null)
    // FIXME: This is an expensive operation, consider doing this in a group by query.
    {
      localesSizeMap = messageList().stream().collect(groupingBy(m -> m.key.id, counting()));
    }

    return localesSizeMap.getOrDefault(keyId, 0L);
  }

  private List<Message> messageList() {
    if (messages == null) {
      messages = Play.current().injector().instanceOf(MessageService.class)
          .findBy(new MessageCriteria().withProjectId(this.id)).getList();
    }

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

  public Project withOwner(User owner) {
    this.owner = owner;
    return this;
  }

  public Project withDeleted(boolean deleted) {
    this.deleted = deleted;
    return this;
  }

  public Project withWhenCreated(DateTime whenCreated) {
    this.whenCreated = whenCreated;
    return this;
  }

  public Project withWhenUpdated(DateTime whenUpdated) {
    this.whenUpdated = whenUpdated;
    return this;
  }

  public Project withMembers(ProjectUser... members) {
    if (this.members == null) {
      this.members = new ArrayList<>();
    }

    this.members.addAll(asList(members));
    return this;
  }

  @Override
  public Project updateFrom(Project in) {
    name = in.name;
    owner = in.owner;

    return this;
  }

  public boolean hasRolesAny(User user, ProjectRole... roles) {
    Map<UUID, List<ProjectRole>> userMap =
        members.stream().collect(groupingBy(m -> m.user.id, mapping(m -> m.role, toList())));
    if (!userMap.containsKey(user.id)) {
      return false;
    }

    List<ProjectRole> userRoles = userMap.get(user.id);
    userRoles.retainAll(asList(roles));

    return !userRoles.isEmpty();
  }

  public ProjectRole roleOf(User user) {
    if (user == null) {
      return null;
    }

    if (user.equals(owner)) {
      return ProjectRole.Owner;
    }

    Optional<ProjectUser> member = members.stream().filter(m -> user.equals(m.user)).findFirst();
    if (member.isPresent()) {
      return member.get().role;
    }

    return null;
  }

  public static UUID brandProjectId() {
    UUID brandProjectId = ContextKey.BrandProjectId.get();
    if (brandProjectId != null) {
      return brandProjectId;
    }

    User user = User.loggedInUser();
    if (user == null) {
      return null;
    }

    Project brandProject = null;
    try {
      brandProject = Play.current().injector().instanceOf(ProjectService.class)
          .byOwnerAndName(user.username, "Translatr");
    } catch (Exception e) {
      LOGGER.warn("Error while retrieving brand project", e);
    }

    if (brandProject == null) {
      return null;
    }

    ContextKey.BrandProjectId.put(Context.current(), brandProject.id);

    return brandProject.id;
  }

  public static String getCacheKey(UUID projectId, String... fetches) {
    return CacheUtils.getCacheKey("project:id", projectId, fetches);
  }

  public static String getCacheKey(String username, String projectName, String... fetches) {
    if (username == null) {
      return null;
    }

    return CacheUtils
        .getCacheKey("project:owner", String.format("%s:%s", username, projectName), fetches);
  }

  /**
   * Return the route to this project.
   */
  public Call route() {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .projectBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  /**
   * Return the edit route to this project.
   */
  public Call editRoute() {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .editBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  /**
   * Return the do edit route to this project.
   */
  public Call doEditRoute() {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .doEditBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  /**
   * Return the removeAll route to this project.
   */
  public Call removeRoute() {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .removeBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  /**
   * Return the route to the locales of this project.
   */
  public Call localesRoute() {
    return localesRoute(AbstractController.DEFAULT_SEARCH, AbstractController.DEFAULT_ORDER,
        AbstractController.DEFAULT_LIMIT, AbstractController.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the locales of this project.
   */
  public Call localesRoute(String search, String order, int limit, int offset) {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .localesBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"), search, order, limit,
            offset);
  }

  /**
   * Return the route to the keys of this project.
   */
  public Call keysRoute() {
    return keysRoute(AbstractController.DEFAULT_SEARCH, AbstractController.DEFAULT_ORDER,
        AbstractController.DEFAULT_LIMIT, AbstractController.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the keys of this project.
   */
  public Call keysRoute(String search) {
    return keysRoute(search, AbstractController.DEFAULT_ORDER, AbstractController.DEFAULT_LIMIT,
        AbstractController.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the keys of this project.
   */
  public Call keysRoute(String search, String order, int limit, int offset) {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .keysBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"), search, order, limit, offset);
  }

  /**
   * Return the route to the members of this project.
   */
  public Call membersRoute() {
    return membersRoute(AbstractController.DEFAULT_SEARCH, AbstractController.DEFAULT_ORDER,
        AbstractController.DEFAULT_LIMIT, AbstractController.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the members of this project.
   */
  private Call membersRoute(String search, String order, int limit, int offset) {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .membersBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"), search, order, limit,
            offset);
  }

  /**
   * Return the route to the members of this project.
   */
  public Call activityRoute() {
    return activityRoute(AbstractController.DEFAULT_SEARCH, AbstractController.DEFAULT_ORDER,
        AbstractController.DEFAULT_LIMIT, AbstractController.DEFAULT_OFFSET);
  }

  /**
   * Return the route to the activity of this project.
   */
  private Call activityRoute(String search, String order, int limit, int offset) {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .activityBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"), search, order, limit,
            offset);
  }

  /**
   * Return the route to the members of this project.
   */
  public Call activityCsvRoute() {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .activityCsvBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  /**
   * Return the route to the word count reset of this project.
   */
  public Call wordCountResetRoute() {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .wordCountResetBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  /**
   * Return the route to the do owner change of this project.
   */
  public Call doOwnerChangeRoute() {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .doOwnerChangeBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  /**
   * Return the route to the do member add of this project.
   */
  public Call doMemberAddRoute() {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .doMemberAddBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"));
  }

  /**
   * Return the route to the member removeAll of this project.
   */
  public Call memberRemoveRoute(Long memberId) {
    Objects.requireNonNull(owner, "Owner is null");
    return controllers.routes.Projects
        .memberRemoveBy(Objects.requireNonNull(owner.username, "Owner username is null"),
            Objects.requireNonNull(name, "Name is null"),
            Objects.requireNonNull(memberId, "Member ID is null"));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Project project = (Project) o;
    return id.equals(project.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return String.format("{\"name\": %s, \"owner\": %s}", Json.toJson(name), owner);
  }
}
