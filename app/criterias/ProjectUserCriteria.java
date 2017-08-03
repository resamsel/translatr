package criterias;

import forms.SearchForm;

/**
 *
 * @author resamsel
 * @version 5 Oct 2016
 */
public class ProjectUserCriteria extends AbstractSearchCriteria<ProjectUserCriteria>
{

	public ProjectUserCriteria() {
		super("projectUser");
	}

	public static ProjectUserCriteria from(SearchForm search)
	{
		return new ProjectUserCriteria().with(search);
	}
}
