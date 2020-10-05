package criterias;

import forms.SearchForm;
import models.UserRole;
import play.mvc.Http.Request;

/**
 * @author resamsel
 * @version 24 Jan 2017
 */
public class UserCriteria extends AbstractSearchCriteria<UserCriteria> {

  private UserRole role;

  public UserCriteria() {
    super("user");
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public UserCriteria withRole(UserRole role) {
    setRole(role);
    return self;
  }

  public static UserCriteria from(Request request) {
    return new UserCriteria()
            .with(request)
            .withRole(request.queryString("role").map(UserRole::fromString).orElse(null));
  }

  public static UserCriteria from(SearchForm form) {
    return new UserCriteria().with(form);
  }

  @Override
  protected String getCacheKeyParticle() {
    return "";
  }
}
