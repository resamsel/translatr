package criterias;

import forms.SearchForm;
import models.ProjectRole;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author resamsel
 * @version 5 Oct 2016
 */
public class ProjectUserCriteria extends AbstractProjectSearchCriteria<ProjectUserCriteria> {

  private final List<ProjectRole> roles = new ArrayList<>();

  public ProjectUserCriteria() {
    super("member");
  }

  public List<ProjectRole> getRoles() {
    return roles;
  }

  public ProjectUserCriteria withRoles(ProjectRole... roles) {
    return withRoles(Arrays.asList(roles));
  }

  public ProjectUserCriteria withRoles(List<ProjectRole> roles) {
    this.roles.addAll(roles);
    return this;
  }

  public static ProjectUserCriteria from(SearchForm search) {
    return new ProjectUserCriteria().with(search);
  }

  public static ProjectUserCriteria from(Http.Request request) {
    return new ProjectUserCriteria().with(request);
  }

  @Override
  protected String getCacheKeyParticle() {
    return "";
  }
}
