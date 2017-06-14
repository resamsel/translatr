package models;

import static utils.Stopwatch.log;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.google.common.collect.ImmutableMap;

import criterias.HasNextPagedList;
import criterias.MessageCriteria;
import play.libs.Json;
import utils.QueryUtils;
import validators.LocaleKeyCheck;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"locale_id", "key_id"})})
@LocaleKeyCheck
public class Message implements Model<Message, UUID> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Message.class);

  public static final String FETCH_LOCALE = "locale";

  public static final String FETCH_KEY = "key";

  private static final List<String> PROPERTIES_TO_FETCH = Arrays.asList(FETCH_LOCALE, FETCH_KEY);

  private static final Map<String, List<String>> FETCH_MAP = ImmutableMap.of(FETCH_LOCALE,
      Arrays.asList(FETCH_LOCALE, FETCH_LOCALE + ".project"), FETCH_KEY, Arrays.asList(FETCH_KEY));

  private static final Find<UUID, Message> find = new Find<UUID, Message>() {};

  @Id
  @GeneratedValue
  public UUID id;

  @Version
  public Long version;

  @CreatedTimestamp
  public DateTime whenCreated;

  @UpdatedTimestamp
  public DateTime whenUpdated;

  @ManyToOne(optional = false)
  public Locale locale;

  @ManyToOne(optional = false)
  public Key key;

  @Column(nullable = false, length = 1024 * 1024)
  public String value;

  public Integer wordCount;

  public Message() {}

  public Message(Locale locale, Key key, String value) {
    this.locale = locale;
    this.key = key;
    this.value = value;
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
  public Message updateFrom(Message in) {
    value = in.value;

    return this;
  }

  /**
   * @param fromString
   * @return
   */
  public static Message byId(UUID id, String... fetches) {
    return QueryUtils
        .fetch(find.setId(id), QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP)
        .findUnique();
  }

  /**
   * @param collect
   * @return
   */
  public static Map<UUID, Message> byIds(List<UUID> ids) {
    return QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where().idIn(ids)
        .findMap();
  }

  /**
   * @param key
   * @param locale
   * @return
   */
  public static Message byKeyAndLocale(Key key, Locale locale) {
    return QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where().eq("key", key)
        .eq("locale", locale).findUnique();
  }

  /**
   * @param project
   * @return
   */
  public static int countBy(Project project) {
    return log(() -> find.where().eq("key.project", project).findCount(), LOGGER,
        "countBy(Project)");
  }

  /**
   * @param key
   * @return
   */
  public static int countBy(Key key) {
    return log(() -> find.where().eq("key", key).findCount(), LOGGER, "countBy(Key)");
  }

  /**
   * @param locale
   * @return
   */
  public static int countBy(Locale locale) {
    return log(() -> find.where().eq("locale", locale).findCount(), LOGGER, "countBy(Locale)");
  }

  /**
   * @param criteria
   * @return
   */
  public static PagedList<Message> findBy(MessageCriteria criteria) {
    ExpressionList<Message> query =
        QueryUtils.fetch(find.query(), criteria.getFetches(), FETCH_MAP).where();

    if (criteria.getProjectId() != null)
      query.eq("key.project.id", criteria.getProjectId());

    if (criteria.getLocaleId() != null)
      query.eq("locale.id", criteria.getLocaleId());

    if (criteria.getLocaleIds() != null)
      query.in("locale.id", criteria.getLocaleIds());

    if (StringUtils.isNotEmpty(criteria.getKeyName()))
      query.eq("key.name", criteria.getKeyName());

    if (criteria.getKeyIds() != null)
      query.in("key.id", criteria.getKeyIds());

    if (StringUtils.isNotEmpty(criteria.getSearch()))
      query.ilike("value", "%" + criteria.getSearch() + "%");

    criteria.paged(query);

    return log(() -> HasNextPagedList.create(query), LOGGER, "findBy");
  }

  /**
   * @param localeId
   * @return
   */
  public static List<Message> byLocale(UUID localeId) {
    return QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where()
        .eq("locale.id", localeId).findList();
  }

  /**
   * @param localeId
   * @return
   */
  public static List<Message> byLocales(Collection<UUID> ids) {
    return QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where()
        .in("locale.id", ids).findList();
  }

  /**
   * @param key
   * @return
   */
  public static List<Message> byKey(Key key) {
    return QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where().eq("key", key)
        .findList();
  }

  /**
   * @param key
   * @return
   */
  public static List<Message> byKeys(Collection<UUID> ids) {
    return find.where().in("key.id", ids).findList();
  }

  /**
   * @param localeId
   * @param key
   * @return
   */
  public static Message byLocaleAndKeyName(UUID localeId, String key) {
    return find.where().eq("locale.id", localeId).eq("key.name", key).findUnique();
  }

  public static List<Message> last(Project project, int limit) {
    return find.fetch("key").fetch("locale").where().eq("key.project", project)
        .order("whenUpdated desc").setMaxRows(limit).findList();
  }

  /**
   * @param messageId
   * @param fetches
   * @return
   */
  public static String getCacheKey(UUID messageId, String... fetches) {
    if (messageId == null)
      return null;

    if (fetches.length > 0)
      return String.format("message:%s:%s", messageId, StringUtils.join(fetches, ":"));

    return String.format("message:%s", messageId);
  }

  @Override
  public String toString() {
    return String.format("{\"locale\": %s, \"key\": %s, \"value\": %s}", locale, key,
        Json.toJson(value));
  }
}
