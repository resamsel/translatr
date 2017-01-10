package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.joda.time.DateTime;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;

import criterias.LogEntryCriteria;
import dto.Dto;
import play.libs.Json;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Entity
public class LogEntry implements Model<LogEntry> {
  @Id
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
   * @return the type
   */
  public ActionType getType() {
    return type;
  }

  private static final Find<UUID, LogEntry> find = new Find<UUID, LogEntry>() {};

  /**
   * @param type
   * @param clazz
   * @param after
   * @param before
   * @return
   */
  public static <T extends Dto> LogEntry from(ActionType type, Project project, Class<T> clazz,
      T before, T after) {
    LogEntry out = new LogEntry();

    out.type = type;
    out.project = project;
    out.contentType = clazz.getName();
    if (before != null)
      out.before = Json.stringify(Json.toJson(before));
    if (after != null)
      out.after = Json.stringify(Json.toJson(after));

    return out;
  }

  /**
   * @param id
   * @return
   */
  public static LogEntry byId(UUID id) {
    return find.setId(id).findUnique();
  }

  /**
   * @param criteria
   * @return
   */
  public static List<LogEntry> findBy(LogEntryCriteria criteria) {
    ExpressionList<LogEntry> query = findQuery(criteria);

    if (criteria.getLimit() != null)
      query.setMaxRows(criteria.getLimit() + 1);

    if (criteria.getOffset() != null)
      query.setFirstRow(criteria.getOffset());

    if (criteria.getOrder() != null)
      query.order(criteria.getOrder());
    else
      query.order("whenCreated desc");

    return query.query().fetch("user").fetch("project").findList();
  }

  public static int countBy(LogEntryCriteria criteria) {
    return findQuery(criteria).findRowCount();
  }

  /**
   * @param criteria
   * @return
   */
  private static ExpressionList<LogEntry> findQuery(LogEntryCriteria criteria) {
    ExpressionList<LogEntry> query = find.where();

    if (criteria.getUserId() != null)
      query.eq("user.id", criteria.getUserId());

    if (criteria.getUserIdExcluded() != null)
      query.ne("user.id", criteria.getUserIdExcluded());

    if (criteria.getProjectId() != null)
      query.eq("project.id", criteria.getProjectId());

    if (criteria.getProjectUserId() != null)
      query.eq("project.members.user.id", criteria.getProjectUserId());

    if (criteria.getWhenCreatedMin() != null)
      query.ge("whenCreated", criteria.getWhenCreatedMin());

    if (criteria.getWhenCreatedMax() != null)
      query.le("whenCreated", criteria.getWhenCreatedMax());

    return query;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogEntry updateFrom(LogEntry in) {
    type = in.type;
    contentType = in.contentType;
    user = in.user;
    project = in.project;
    before = in.before;
    after = in.after;

    return this;
  }
}
