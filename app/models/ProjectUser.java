package models;

import static utils.Stopwatch.log;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import criterias.HasNextPagedList;
import criterias.ProjectUserCriteria;
import utils.QueryUtils;

/**
 * @author resamsel
 * @version 5 Oct 2016
 */
@Entity
public class ProjectUser implements Model<ProjectUser, Long> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectUser.class);

  public static final int ROLE_LENGTH = 16;

  private static final String FETCH_PROJECT = "project";

  private static final List<String> PROPERTIES_TO_FETCH = Arrays.asList(FETCH_PROJECT);

  private static final Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of(FETCH_PROJECT, Arrays.asList(FETCH_PROJECT, FETCH_PROJECT + ".owner"));


  @Id
  @GeneratedValue
  public Long id;

  @Version
  public Long version;

  @JsonIgnore
  @CreatedTimestamp
  public DateTime whenCreated;

  @JsonIgnore
  @UpdatedTimestamp
  public DateTime whenUpdated;

  @ManyToOne
  public Project project;

  @ManyToOne
  public User user;

  @Enumerated(EnumType.STRING)
  @Column(length = ROLE_LENGTH)
  public ProjectRole role;

  /**
   *
   */
  public ProjectUser() {
  }

  /**
   *
   */
  public ProjectUser(ProjectRole role) {
    this.role = role;
  }

  private static final Find<Long, ProjectUser> find = new Find<Long, ProjectUser>() {
  };

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getId() {
    return id;
  }

  public ProjectUser withProject(Project project) {
    this.project = project;
    return this;
  }

  public ProjectUser withUser(User user) {
    this.user = user;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ProjectUser updateFrom(ProjectUser in) {
    project = in.project;
    user = in.user;
    role = in.role;

    return this;
  }

  public static ProjectUser byId(Long id) {
    return QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where().idEq(id)
        .findUnique();
  }

  /**
   * @param criteria
   * @return
   */
  private static ExpressionList<ProjectUser> findQuery(ProjectUserCriteria criteria) {
    ExpressionList<ProjectUser> query = find.where();

    query.eq("project.deleted", false);

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    criteria.paged(query);

    return query;
  }

  public static PagedList<ProjectUser> findBy(ProjectUserCriteria criteria) {
    return log(() -> HasNextPagedList.create(findQuery(criteria).query().fetch("user")), LOGGER,
        "findBy");
  }

  public static int countBy(ProjectUserCriteria criteria) {
    return log(() -> findQuery(criteria).findCount(), LOGGER, "countBy");
  }

  /**
   * @param project
   * @return
   */
  public static long countBy(Project project) {
    return find.where().eq("project", project).findCount();
  }
}
