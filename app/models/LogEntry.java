package models;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.annotation.CreatedTimestamp;
import criterias.HasNextPagedList;
import criterias.LogEntryCriteria;
import dto.Dto;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import play.libs.Json;
import utils.QueryUtils;

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


  private static final Find<UUID, LogEntry> find = new Find<UUID, LogEntry>() {
  };

  private static final List<String> PROPERTIES_TO_FETCH = Arrays.asList("user", "project");

  /**
   * @param type
   * @param clazz
   * @param after
   * @param before
   * @return
   */
  public static <T extends Dto> LogEntry from(ActionType type, Project project, Class<T> clazz,
      T before, T after) {
    return from(type, null, project, clazz, before, after);
  }

  /**
   * @param type
   * @param user
   * @param project
   * @param clazz
   * @param before
   * @param after
   * @return
   */
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
   * @param id
   * @return
   */
  public static LogEntry byId(UUID id, String... fetches) {
    HashSet<String> propertiesToFetch = new HashSet<>(PROPERTIES_TO_FETCH);
    if (fetches.length > 0) {
      propertiesToFetch.addAll(Arrays.asList(fetches));
    }

    return QueryUtils.fetch(find.setId(id).setDisableLazyLoading(true), propertiesToFetch)
        .findUnique();
  }

  /**
   * @param criteria
   * @return
   */
  public static PagedList<LogEntry> findBy(LogEntryCriteria criteria) {
    ExpressionList<LogEntry> query = findQuery(criteria);

    if (criteria.getOrder() != null) {
      query.order(criteria.getOrder());
    } else {
      query.order("whenCreated desc");
    }

    criteria.paged(query);

    return HasNextPagedList.create(query.query().fetch("user").fetch("project"));
  }

  public static int countBy(LogEntryCriteria criteria) {
    return findQuery(criteria).findCount();
  }

  /**
   * @param criteria
   * @return
   */
  private static ExpressionList<LogEntry> findQuery(LogEntryCriteria criteria) {
    ExpressionList<LogEntry> query = find.where();

    if (criteria.getIds() != null) {
      query.idIn(criteria.getIds());
    }

    if (StringUtils.isNoneEmpty(criteria.getSearch())) {
      query.disjunction().ilike("before", "%" + criteria.getSearch() + "%")
          .ilike("after", "%" + criteria.getSearch() + "%").endJunction();
    }

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

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

  /**
   * @param activityId
   * @param fetches
   * @return
   */
  public static String getCacheKey(UUID activityId, String... fetches) {
    if (activityId == null) {
      return null;
    }

    if (fetches.length > 0) {
      return String.format("activity:%s:%s", activityId, StringUtils.join(fetches, ":"));
    }

    return String.format("activity:%s", activityId);
  }

  /**
   * @return
   */
  public String getSimpleContentType() {
    return contentType.replaceAll("^.*\\.", "").toLowerCase();
  }
}
