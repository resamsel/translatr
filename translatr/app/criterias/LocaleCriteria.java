package criterias;

import forms.LocaleSearchForm;
import play.mvc.Http.Request;

/**
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LocaleCriteria extends AbstractSearchCriteria<LocaleCriteria> {
  public static final String PARAM_LOCALE_NAME = "localeName";
  public static final String PARAM_MESSAGES_KEY_NAME = "messages.keyName";

  private Boolean missing;

  private String localeName;

  private String messagesKeyName;

  public Boolean getMissing() {
    return missing;
  }

  public void setMissing(Boolean missing) {
    this.missing = missing;
  }

  public LocaleCriteria withMissing(Boolean missing) {
    setMissing(missing);
    return this;
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

  public void setMessagesKeyName(String messagesKeyName) {
    this.messagesKeyName = messagesKeyName;
  }

  public LocaleCriteria withMessagesKeyName(String messagesKeyName) {
    setMessagesKeyName(messagesKeyName);
    return this;
  }

  public static LocaleCriteria from(LocaleSearchForm form) {
    return new LocaleCriteria().with(form).withMissing(form.missing);
  }

  public static LocaleCriteria from(Request request) {
    return new LocaleCriteria().with(request)
        .withLocaleName(request.getQueryString(PARAM_LOCALE_NAME))
        .withMessagesKeyName(request.getQueryString(PARAM_MESSAGES_KEY_NAME));
  }
}
