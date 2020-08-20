package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import play.libs.Json;
import play.mvc.Http.Context;
import utils.CacheUtils;
import utils.UrlUtils;
import validators.KeyNameUniqueChecker;
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
@NameUnique(checker = KeyNameUniqueChecker.class)
public class Key implements Model<Key, UUID>, Suggestable {

  public static final int NAME_LENGTH = 255;

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

  @Column(nullable = false)
  @NotNull
  public String name;

  public Integer wordCount;

  @Transient
  public Double progress;

  @JsonIgnore
  @OneToMany
  public List<Message> messages;

  public Key() {
  }

  public Key(Project project, String name) {
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

  /**
   * {@inheritDoc}
   */
  @Override
  public String value() {
    return Context.current().messages().at("key.autocomplete", name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Data data() {
    return Data.from(Key.class, id, name, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Key updateFrom(Key in) {
    project = project.updateFrom(in.project);
    name = ObjectUtils.firstNonNull(in.name, name);

    return this;
  }

  public static String getCacheKey(UUID keyId, String... fetches) {
    return CacheUtils.getCacheKey("key:id", keyId, fetches);
  }

  @Override
  public String toString() {
    return String.format("{\"project\": %s, \"name\": %s}", project, Json.toJson(name));
  }

  public String getPathName() {
    return UrlUtils.encode(name);
  }
}
