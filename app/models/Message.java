package models;

import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import org.joda.time.DateTime;
import play.libs.Json;
import utils.CacheUtils;
import validators.LocaleKeyCheck;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"locale_id", "key_id"})})
@LocaleKeyCheck
public class Message implements Model<Message, UUID> {

  @Id
  @GeneratedValue
  public UUID id;

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
    id = ofNullable(in.id).orElse(id);
    locale = ofNullable(locale).map(l -> l.updateFrom(in.locale)).orElse(in.locale);
    key = ofNullable(key).map(k -> k.updateFrom(in.key)).orElse(in.key);
    value = in.value;

    return this;
  }

  public static String getCacheKey(UUID messageId, String... fetches) {
    return CacheUtils.getCacheKey("message:id", messageId, fetches);
  }

  @Override
  public String toString() {
    return String.format("{\"locale\": %s, \"key\": %s, \"value\": %s}", locale, key,
            Json.toJson(value));
  }
}
