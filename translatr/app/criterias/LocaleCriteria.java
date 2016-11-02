package criterias;

import forms.LocaleSearchForm;

/**
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LocaleCriteria extends AbstractSearchCriteria<LocaleCriteria> {
  private Boolean missing;

  private String localeName;

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

  public static LocaleCriteria from(LocaleSearchForm form) {
    return new LocaleCriteria().with(form).withMissing(form.missing);
  }
}
