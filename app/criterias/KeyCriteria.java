package criterias;

import forms.KeySearchForm;
import forms.SearchForm;
import play.mvc.Http.Request;
import utils.JsonUtils;

import java.util.Collection;
import java.util.UUID;

import static controllers.AbstractBaseApi.PARAM_LOCALE_ID;
import static controllers.AbstractBaseApi.PARAM_MISSING;

/**
 * @author resamsel
 * @version 19 Aug 2016
 */
public class KeyCriteria extends AbstractProjectSearchCriteria<KeyCriteria> {

  private Boolean missing;

  private Collection<String> names;

  private UUID localeId;

  private String projectOwnerUsername;

  private String projectName;

  public KeyCriteria() {
    super("key");
  }

  public Boolean getMissing() {
    return missing;
  }

  public void setMissing(Boolean missing) {
    this.missing = missing;
  }

  private KeyCriteria withMissing(Boolean missing) {
    setMissing(missing);
    return this;
  }

  public Collection<String> getNames() {
    return names;
  }

  public void setNames(Collection<String> names) {
    this.names = names;
  }

  public KeyCriteria withNames(Collection<String> names) {
    setNames(names);
    return this;
  }

  public UUID getLocaleId() {
    return localeId;
  }

  public void setLocaleId(UUID localeId) {
    this.localeId = localeId;
  }

  public static KeyCriteria from(Request request) {
    return new KeyCriteria().with(request)
        .withMissing(Boolean.parseBoolean(request.queryString().getOrDefault(PARAM_MISSING, new String[]{"false"})[0]))
        .withLocaleId(JsonUtils.getUuid(request.getQueryString(PARAM_LOCALE_ID)));
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

  @Override
  protected String getCacheKeyParticle() {
    return String
        .format("%s:%s:%s:%s:%s", projectOwnerUsername, projectName, missing, names, localeId);
  }

  public static KeyCriteria from(SearchForm form) {
    return new KeyCriteria().with(form);
  }

  public static KeyCriteria from(KeySearchForm form) {
    return new KeyCriteria().with(form).withMissing(form.missing);
  }

  private KeyCriteria withLocaleId(UUID localeId) {
    setLocaleId(localeId);
    return this;
  }
}
