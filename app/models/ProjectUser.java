package models;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.AbstractController;
import controllers.routes;
import org.joda.time.DateTime;
import play.mvc.Call;
import utils.CacheUtils;
import validators.NameUnique;
import validators.ProjectUserUniqueChecker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Objects;

/**
 * @author resamsel
 * @version 5 Oct 2016
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "user_id"})})
@NameUnique(checker = ProjectUserUniqueChecker.class, field = "user", message = "error.projectuserunique")
public class ProjectUser implements Model<ProjectUser, Long> {

  private static final int ROLE_LENGTH = 16;

  public static final String FETCH_PROJECT = "project";

  @Id
  @GeneratedValue
  public Long id;

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

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getId() {
    return id;
  }

  public ProjectUser withId(Long id) {
    this.id = id;
    return this;
  }

  public ProjectUser withProject(Project project) {
    this.project = project;
    return this;
  }

  public ProjectUser withUser(User user) {
    this.user = user;
    return this;
  }

  public ProjectUser withRole(ProjectRole role) {
    this.role = role;
    return this;
  }

  public ProjectUser withWhenCreated(DateTime whenCreated) {
    this.whenCreated = whenCreated;
    return this;
  }

  public ProjectUser withWhenUpdated(DateTime whenUpdated) {
    this.whenUpdated = whenUpdated;
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

  public static String getCacheKey(Long id, String... fetches) {
    return CacheUtils.getCacheKey("member:id", id, fetches);
  }

  public Call route() {
    Objects.requireNonNull(project, "Project is null");
    Objects.requireNonNull(project.owner, "Project owner is null");

    return routes.Projects.membersBy(project.owner.username, project.name,
        AbstractController.DEFAULT_SEARCH, AbstractController.DEFAULT_ORDER,
        AbstractController.DEFAULT_LIMIT, AbstractController.DEFAULT_OFFSET);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ProjectUser that = (ProjectUser) o;
    return project.equals(that.project) &&
        user.equals(that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(project, user);
  }
}
