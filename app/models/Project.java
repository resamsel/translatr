package models;

import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.AbstractController;
import criterias.MessageCriteria;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Play;
import play.api.mvc.Call;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Context;
import services.MessageService;
import services.ProjectService;
import utils.CacheUtils;
import utils.ContextKey;
import validators.NameUnique;
import validators.ProjectName;
import validators.ProjectNameUniqueChecker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
  @Constraints.MaxLength(255)
  @ProjectName
  public String name;

  @Constraints.MaxLength(2000)
  public String description;

  public Integer wordCount;

  @Transient
  public Double progress;

  @JsonIgnore
  @OneToMany
  public List<Locale> locales;

  @JsonIgnore
  @OneToMany
  public List<Key> keys;

  @JsonIgnore
//  @OneToMany(cascade = CascadeType.PERSIST)
  @OneToMany
  public List<ProjectUser> members;

  @Transient
  @Enumerated(EnumType.STRING)
  public ProjectRole myRole;

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
    return Data.from(Project.class, id, name, null);
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
    name = ObjectUtils.firstNonNull(in.name, name);
    description = in.description;
    owner = ObjectUtils.firstNonNull(in.owner, owner);

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

    return members.stream()
        .filter(m -> user.equals(m.user))
        .findFirst()
        .map(projectUser -> projectUser.role)
        .orElse(null);
  }

  public static UUID brandProjectId(User loggedInUser, Http.Context ctx) {
    UUID brandProjectId = ContextKey.BrandProjectId.get(ctx);
    if (brandProjectId != null) {
      return brandProjectId;
    }

    if (loggedInUser == null) {
      return null;
    }

    Project brandProject = null;
    try {
      brandProject = Play.current().injector().instanceOf(ProjectService.class)
          .byOwnerAndName(loggedInUser.username, "Translatr");
    } catch (Exception e) {
      LOGGER.warn("Error while retrieving brand project", e);
    }

    if (brandProject == null) {
      return null;
    }

    ContextKey.BrandProjectId.put(ctx, brandProject.id);

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
