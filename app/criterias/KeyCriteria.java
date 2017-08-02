package criterias;

import forms.KeySearchForm;
import forms.SearchForm;
import java.util.Collection;
import java.util.UUID;
import play.mvc.Http.Request;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public class KeyCriteria extends AbstractSearchCriteria<KeyCriteria> {

  private Boolean missing;

  private Collection<String> names;

  private UUID localeId;

  private String projectOwnerUsername;

  private String projectName;

  public Boolean getMissing() {
    return missing;
  }

  public void setMissing(Boolean missing) {
    this.missing = missing;
  }

  public KeyCriteria withMissing(Boolean missing) {
    setMissing(missing);
    return this;
  }

  /**
   * @return the names
   */
  public Collection<String> getNames() {
    return names;
  }

  /**
   * @param names the names to set
   */
  public void setNames(Collection<String> names) {
    this.names = names;
  }

  /**
   * @param names
   * @return
   */
  public KeyCriteria withNames(Collection<String> names) {
    setNames(names);
    return this;
  }

  /**
   * @return the untranslated
   */
  public UUID getLocaleId() {
    return localeId;
  }

  /**
   * @param localeId the untranslated to set
   */
  public void setLocaleId(UUID localeId) {
    this.localeId = localeId;
  }

  /**
   * @param localeId
   * @return
   */
  public KeyCriteria withLocaleId(String localeId) {
    if (localeId != null) {
      setLocaleId(UUID.fromString(localeId));
    }
    return this;
  }

  public String getProjectOwnerUsername() {
    return projectOwnerUsername;
  }

  public void setProjectOwnerUsername(String projectOwnerUsername) {
    this.projectOwnerUsername = projectOwnerUsername;
  }

  public KeyCriteria withProjectOwnerUsername(String projectOwnerUsername) {
    setProjectOwnerUsername(projectOwnerUsername);
    return this;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public KeyCriteria withProjectName(String projectName) {
    setProjectName(projectName);
    return this;
  }

  /**
   * @param form
   * @return
   */
  public static KeyCriteria from(SearchForm form) {
    return new KeyCriteria().with(form);
  }

  /**
   * @param form
   * @return
   */
  public static KeyCriteria from(KeySearchForm form) {
    return new KeyCriteria().with(form).withMissing(form.missing);
  }

  public static KeyCriteria from(Request request) {
    return new KeyCriteria().with(request).withMissing(Boolean
        .parseBoolean(request.queryString().getOrDefault("missing", new String[]{"false"})[0]))
        .withLocaleId(request.queryString().getOrDefault("localeId", new String[]{null})[0]);
  }
}
