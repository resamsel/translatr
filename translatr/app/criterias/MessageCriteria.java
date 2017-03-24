package criterias;

import java.util.List;
import java.util.UUID;

import play.mvc.Http.Request;

/**
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class MessageCriteria extends AbstractSearchCriteria<MessageCriteria> {
  private UUID localeId;

  private String keyName;

  private List<UUID> localeIds;

  private List<UUID> keyIds;

  public List<UUID> getLocaleIds() {
    return localeIds;
  }

  public void setLocaleIds(List<UUID> localeIds) {
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

  /**
   * @param localeId
   * @return
   */
  public MessageCriteria withLocaleId(UUID localeId) {
    setLocaleId(localeId);
    return this;
  }

  /**
   * @return the keyIds
   */
  public List<UUID> getKeyIds() {
    return keyIds;
  }

  /**
   * @param keyIds the keyIds to set
   */
  public void setKeyIds(List<UUID> keyIds) {
    this.keyIds = keyIds;
  }

  /**
   * @param keyIds
   * @return
   */
  public MessageCriteria withKeyIds(List<UUID> keyIds) {
    setKeyIds(keyIds);
    return this;
  }

  public static MessageCriteria from(Request request) {
    return new MessageCriteria().with(request);
  }
}
