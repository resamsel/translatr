package criterias;

import forms.SearchForm;
import play.mvc.Http;

/**
 * @author resamsel
 * @version 5 Oct 2016
 */
public class ProjectUserCriteria extends AbstractProjectSearchCriteria<ProjectUserCriteria> {

  public ProjectUserCriteria() {
    super("member");
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
