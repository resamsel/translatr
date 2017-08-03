package models;

import static utils.Stopwatch.log;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.google.common.collect.ImmutableMap;
import criterias.HasNextPagedList;
import criterias.MessageCriteria;
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

  public Message() {
  }

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

  public static String getCacheKey(UUID messageId, String... fetches) {
    if (messageId == null) {
      return null;
    }

    if (fetches.length > 0) {
      return String.format("message:%s:%s", messageId, StringUtils.join(fetches, ":"));
    }

    return String.format("message:%s", messageId);
  }

  @Override
  public String toString() {
    return String.format("{\"locale\": %s, \"key\": %s, \"value\": %s}", locale, key,
        Json.toJson(value));
  }
}
