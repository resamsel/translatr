package models;

import io.ebean.annotation.CreatedTimestamp;
import org.joda.time.DateTime;
import play.libs.Json;
import utils.CacheUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Entity
public class LogEntry implements Model<LogEntry, UUID> {

  @Id
  @GeneratedValue
  public UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public ActionType type;

  @Column(nullable = false, length = 64)
  public String contentType;

  @CreatedTimestamp
  @Column(nullable = false)
  public DateTime whenCreated;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  public User user;

  @ManyToOne
  public Project project;

  @Column(length = 1024 * 1024)
  public String before;

  @Column(length = 1024 * 1024)
  public String after;

  /**
   * {@inheritDoc}
   */
  @Override
  public UUID getId() {
    return id;
  }

  /**
   * @return the type
   */
  public ActionType getType() {
    return type;
  }

  public LogEntry withUser(User user) {
    this.user = user;
    return this;
  }

  public static <T> LogEntry from(ActionType type, User user, Project project, Class<T> clazz,
      T before, T after) {
    LogEntry out = new LogEntry();

    out.type = type;
    out.user = user;
    out.project = project;
    out.contentType = clazz.getName();
    if (before != null) {
      out.before = Json.stringify(Json.toJson(before));
    }
    if (after != null) {
      out.after = Json.stringify(Json.toJson(after));
    }

    return out;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogEntry updateFrom(LogEntry in) {
    type = in.type;
    contentType = in.contentType;
    user = user.updateFrom(in.user);
    project = project.updateFrom(in.project);
    before = in.before;
    after = in.after;

    return this;
  }

  public static String getCacheKey(UUID activityId, String... fetches) {
    return CacheUtils.getCacheKey("activity:id", activityId, fetches);
  }

  public String getSimpleContentType() {
    return contentType.replaceAll("^.*\\.", "").toLowerCase();
  }
}
