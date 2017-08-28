package criterias;

import forms.SearchForm;

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

  @Override
  protected String getCacheKeyParticle() {
    return "";
  }
}
