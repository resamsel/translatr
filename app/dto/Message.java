package dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import models.Key;
import models.Locale;
import play.libs.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message extends Dto {
  private static final long serialVersionUID = -8848810325610871318L;

  private static final Logger LOGGER = LoggerFactory.getLogger(Message.class);

  public UUID id;

  @JsonIgnore
  public DateTime whenCreated;

  @JsonIgnore
  public DateTime whenUpdated;

  @NotNull
  public UUID localeId;

  public String localeName;

  @NotNull
  public UUID keyId;

  public String keyName;

  public String value;

  public Message() {}

  private Message(models.Message in) {
    this.id = in.id;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
    this.value = in.value;

    if (in.locale != null) {
      this.localeId = in.locale.id;
      this.localeName = in.locale.name;
    }

    if (in.key != null) {
      this.keyId = in.key.id;
      this.keyName = in.key.name;
    }
  }

  public models.Message toModel(Locale locale, Key key) {
    models.Message out = new models.Message();

    out.id = id;
    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;
    out.locale = locale;
    out.key = key;
    out.value = value;

    LOGGER.trace("DTO Message toModel: {}", Json.toJson(out));

    return out;
  }

  public static Message validate(Message message) {
    return message;
  }

  public static Message from(models.Message message) {
    return validate(new Message(message));
  }
}
