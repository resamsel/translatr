package models;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import criterias.MessageCriteria;
import criterias.ProjectCriteria;
import play.api.Play;
import play.mvc.Http.Context;
import services.ProjectService;
import utils.PermissionUtils;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"owner_id", "name"})})
public class Project implements Suggestable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Project.class);

  public static final int NAME_LENGTH = 255;

  @Id
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
  public String name;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  public User owner;

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
  private Long keysSize;

  @Transient
  private Long localesSize;

  @Transient
  private Map<UUID, Long> keysSizeMap;

  public Project() {}

  public Project(String name) {
    this.name = name;
  }

  @Override
  public String value() {
    return name;
  }

  @Override
  public Data data() {
    return Data.from(Project.class, id, name,
        controllers.routes.Projects.project(id).absoluteURL(Context.current().request()));
  }

  public Project withId(UUID id) {
    this.id = id;
    return this;
  }

  private static final Find<UUID, Project> find = new Find<UUID, Project>() {};

  private static final String BRAND_PROJECT_ID = "brandProjectId";

  public static Project byId(UUID id) {
    return Play.current().injector().instanceOf(ProjectService.class).getById(id);
  }

  public static Project byIdUncached(UUID id) {
    return find.byId(id);
  }

  /**
   * @param criteria
   * @return
   */
  public static List<Project> findBy(ProjectCriteria criteria) {
    ExpressionList<Project> query = find.fetch("owner").where();

    query.eq("deleted", false);

    if (criteria.getOwnerId() != null)
      query.eq("owner.id", criteria.getOwnerId());

    if (criteria.getMemberId() != null)
      query.eq("members.user.id", criteria.getMemberId());

    if (criteria.getProjectId() != null)
      query.eq("id", criteria.getProjectId());

    if (criteria.getSearch() != null)
      query.ilike("name", "%" + criteria.getSearch() + "%");

    criteria.paging(query);

    return log(() -> query.findList(), LOGGER, "findBy");
  }

  /**
   * @return
   */
  public static List<Project> all() {
    return find.where().eq("deleted", false).order("name").findList();
  }

  public float progress() {
    long keysSize = keysSize();
    long localesSize = localesSize();
    if (keysSize < 1 || localesSize < 1)
      return 0f;
    return (float) Message.countBy(this) / (float) (keysSize * localesSize);
  }

  public long missing(UUID localeId) {
    return keysSize() - keysSizeMap(localeId);
  }

  public long keysSizeMap(UUID localeId) {
    if (keysSizeMap == null)
      keysSizeMap = Message.findBy(new MessageCriteria().withProjectId(this.id)).stream()
          .collect(groupingBy(m -> m.locale.id, counting()));

    return keysSizeMap.getOrDefault(localeId, 0l);
  }

  public long localesSize() {
    if (localesSize == null)
      localesSize = Locale.countBy(this);
    return localesSize;
  }

  public long keysSize() {
    if (keysSize == null)
      keysSize = Key.countBy(this);
    return keysSize;
  }

  public long messagesSize() {
    return Message.countBy(this);
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

  /**
   * @param name
   * @return
   */
  public static Project byOwnerAndName(User user, String name) {
    return Play.current().injector().instanceOf(ProjectService.class).getByOwnerAndName(user, name);
  }

  /**
   * @param user
   * @param name
   * @return
   */
  public static Project byOwnerAndNameUncached(User user, String name) {
    return find.where().eq("owner", user).eq("name", name).findUnique();
  }

  /**
   * @param project
   */
  public Project updateFrom(Project in) {
    name = in.name;

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

  public static UUID brandProjectId() {
    Context ctx = Context.current();
    Map<String, Object> args = ctx.args;
    if (args.containsKey(BRAND_PROJECT_ID))
      return (UUID) args.get(BRAND_PROJECT_ID);

    User user = User.loggedInUser();
    if (user == null)
      user = User.byUsername("translatr");

    Project brandProject = null;
    try {
      brandProject = byOwnerAndName(user, ctx.messages().at("brand"));
    } catch (Exception e) {
      LOGGER.warn("Error while retrieving brand project", e);
    }

    if (brandProject == null)
      return null;

    args.put(BRAND_PROJECT_ID, brandProject.id);

    return brandProject.id;
  }

  /**
   * @param user
   * @param role
   * @return
   */
  public boolean hasPermission(User user, ProjectRole role) {
    return PermissionUtils.hasPermissionAny(this, user, role);
  }
}
