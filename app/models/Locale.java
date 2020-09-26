package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import play.libs.Json;
import utils.CacheUtils;
import utils.UrlUtils;
import validators.LocaleNameUniqueChecker;
import validators.NameUnique;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "name"})})
@NameUnique(checker = LocaleNameUniqueChecker.class)
public class Locale implements Model<Locale, UUID> {

  public static final int NAME_LENGTH = 15;

  @Id
  @GeneratedValue
  public UUID id;

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

  @Transient
  public Double progress;

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
  public Locale updateFrom(Locale in) {
    project = project.updateFrom(in.project);
    name = ObjectUtils.firstNonNull(in.name, name);

    return this;
  }

  public static String getCacheKey(UUID localeId, String... fetches) {
    return CacheUtils.getCacheKey("locale:id", localeId, fetches);
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
}
