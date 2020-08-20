package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import org.joda.time.DateTime;
import utils.CacheUtils;
import validators.NameUnique;
import validators.ProjectUserModifyAllowed;
import validators.ProjectUserOwnerExists;
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
@ProjectUserOwnerExists
@ProjectUserModifyAllowed
public class ProjectUser implements Model<ProjectUser, Long> {

  private static final int ROLE_LENGTH = 16;

  public static final String FETCH_PROJECT = "project";
  public static final String FETCH_USER = "user";

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
    project = project.updateFrom(in.project);
    user = user.updateFrom(in.user);
    role = in.role;

    return this;
  }

  public static String getCacheKey(Long id, String... fetches) {
    return CacheUtils.getCacheKey("member:id", id, fetches);
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
