package criterias;

import forms.LocaleSearchForm;
import play.mvc.Http.Request;
import utils.JsonUtils;

import java.util.UUID;

import static controllers.AbstractBaseApi.PARAM_KEY_ID;
import static controllers.AbstractBaseApi.PARAM_MISSING;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LocaleCriteria extends AbstractProjectSearchCriteria<LocaleCriteria> {

  public static final String PARAM_LOCALE_NAME = "localeName";
  public static final String PARAM_MESSAGES_KEY_NAME = "messages.keyName";

  private Boolean missing;

  private UUID keyId;

  private String localeName;

  private String messagesKeyName;

  public LocaleCriteria() {
    super("locale");
  }

  public Boolean getMissing() {
    return missing;
  }

  public void setMissing(Boolean missing) {
    this.missing = missing;
  }

  private LocaleCriteria withMissing(Boolean missing) {
    setMissing(missing);
    return this;
  }

  public static LocaleCriteria from(Request request) {
    return new LocaleCriteria()
        .with(request)
        .withLocaleName(request.getQueryString(PARAM_LOCALE_NAME))
        .withMissing(Boolean.parseBoolean(request.queryString().getOrDefault(PARAM_MISSING, new String[]{"false"})[0]))
        .withKeyId(JsonUtils.getUuid(request.getQueryString(PARAM_KEY_ID)))
        .withMessagesKeyName(request.getQueryString(PARAM_MESSAGES_KEY_NAME));
  }

  public UUID getKeyId() {
    return keyId;
  }

  public void setKeyId(UUID keyId) {
    this.keyId = keyId;
  }

  public String getLocaleName() {
    return localeName;
  }

  public void setLocaleName(String localeName) {
    this.localeName = localeName;
  }

  /**
   * @param localeName the localeName to set
   * @return this
   */
  public LocaleCriteria withLocaleName(String localeName) {
    setLocaleName(localeName);
    return this;
  }

  public String getMessagesKeyName() {
    return messagesKeyName;
  }

  private void setMessagesKeyName(String messagesKeyName) {
    this.messagesKeyName = messagesKeyName;
  }

  private LocaleCriteria withMessagesKeyName(String messagesKeyName) {
    setMessagesKeyName(messagesKeyName);
    return this;
  }

  @Override
  protected String getCacheKeyParticle() {
    return String.format("%s:%s:%s", missing, localeName, messagesKeyName);
  }

  public static LocaleCriteria from(LocaleSearchForm form) {
    return new LocaleCriteria().with(form).withMissing(form.missing);
  }

  private LocaleCriteria withKeyId(UUID keyId) {
    setKeyId(keyId);
    return this;
  }
}
