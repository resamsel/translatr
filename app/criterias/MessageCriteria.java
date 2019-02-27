package criterias;

import java.util.List;
import java.util.UUID;
import play.mvc.Http.Request;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public class MessageCriteria extends AbstractProjectSearchCriteria<MessageCriteria> {

  private UUID localeId;

  private String keyName;

  private List<UUID> localeIds;

  public MessageCriteria() {
    super("message");
  }

  public List<UUID> getLocaleIds() {
    return localeIds;
  }

  private void setLocaleIds(List<UUID> localeIds) {
    this.localeIds = localeIds;
  }

  /**
   * @param localeIds the localeIds to set
   * @return this
   */
  public MessageCriteria withLocaleIds(List<UUID> localeIds) {
    setLocaleIds(localeIds);
    return this;
  }

  /**
   * @return the keyName
   */
  public String getKeyName() {
    return keyName;
  }

  /**
   * @param keyName the keyName to set
   */
  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  /**
   * @param keyName the keyName to set
   * @return this
   */
  public MessageCriteria withKeyName(String keyName) {
    setKeyName(keyName);
    return this;
  }

  /**
   * @return the localeId
   */
  public UUID getLocaleId() {
    return localeId;
  }

  /**
   * @param localeId the localeId to set
   */
  public void setLocaleId(UUID localeId) {
    this.localeId = localeId;
  }

  public MessageCriteria withLocaleId(UUID localeId) {
    setLocaleId(localeId);
    return this;
  }

  @Override
  protected String getCacheKeyParticle() {
    return String.format("%s:%s:%s", localeId, keyName, localeIds);
  }

  public static MessageCriteria from(Request request) {
    return new MessageCriteria().with(request);
  }
}
