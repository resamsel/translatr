package models;

import static utils.Stopwatch.log;

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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.databind.JsonNode;

import criterias.MessageCriteria;
import play.api.Play;
import play.libs.Json;
import services.MessageService;
import validators.LocaleKeyCheck;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"locale_id", "key_id"})})
@LocaleKeyCheck
public class Message implements Model<Message, UUID> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Message.class);

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

  public Message() {}

  public Message(models.Message message) {
    this(message.locale, message.key, message.value);
  }

  public Message(Locale locale, Key key, String value) {
    this.locale = locale;
    this.key = key;
    this.value = value;
  }

  private static final Find<UUID, Message> find = new Find<UUID, Message>() {};

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
  public static Message byId(UUID id) {
    return find.setId(id).fetch("key").fetch("locale").findUnique();
  }

  /**
   * @param collect
   * @return
   */
  public static Map<UUID, Message> byIds(List<UUID> ids) {
    return find.fetch("key").fetch("locale").where().in("id", ids).findMap();
  }

  /**
   * @param key
   * @param locale
   * @return
   */
  public static Message byKeyAndLocale(Key key, Locale locale) {
    return find.fetch("key").fetch("locale").where().eq("key", key).eq("locale", locale)
        .findUnique();
  }

  /**
   * @param project
   * @return
   */
  public static int countBy(Project project) {
    return Play.current().injector().instanceOf(MessageService.class).countBy(project);
  }

  /**
   * @param project
   * @return
   */
  public static int countByUncached(Project project) {
    return find.where().eq("key.project", project).findCount();
  }

  /**
   * @param key
   * @return
   */
  public static int countBy(Key key) {
    return find.where().eq("key", key).findCount();
  }

  /**
   * @param locale
   * @return
   */
  public static int countBy(Locale locale) {
    return find.where().eq("locale", locale).findCount();
  }

  /**
   * @param messageCriteria
   * @return
   */
  public static List<Message> findBy(MessageCriteria criteria) {
    ExpressionList<Message> query = Message.find.fetch("key").fetch("locale").where();

    if (criteria.getProjectId() != null)
      query.eq("key.project.id", criteria.getProjectId());

    if (criteria.getLocaleId() != null)
      query.eq("locale.id", criteria.getLocaleId());

    if (criteria.getKeyName() != null)
      query.eq("key.name", criteria.getKeyName());

    if (criteria.getKeyIds() != null)
      query.in("key.id", criteria.getKeyIds());

    if (criteria.getSearch() != null)
      query.ilike("value", "%" + criteria.getSearch() + "%");

    if (criteria.getOrder() != null)
      query.setOrderBy(criteria.getOrder());

    if (criteria.getLimit() != null)
      query.setMaxRows(criteria.getLimit());

    return log(() -> query.findList(), LOGGER, "findBy");
  }

  /**
   * @param localeId
   * @return
   */
  public static List<Message> byLocale(UUID localeId) {
    return find.fetch("key").fetch("locale").where().eq("locale.id", localeId).findList();
  }

  /**
   * @param localeId
   * @return
   */
  public static List<Message> byLocales(Collection<UUID> ids) {
    return find.where().in("locale.id", ids).findList();
  }

  /**
   * @param key
   * @return
   */
  public static List<Message> byKey(Key key) {
    return find.where().eq("key", key).findList();
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

  public static Message from(JsonNode json) {
    return Json.fromJson(json, dto.Message.class).toModel();
  }

  @Override
  public String toString() {
    return String.format("{\"locale\": %s, \"key\": %s, \"value\": %s}", locale, key,
        Json.toJson(value));
  }
}
