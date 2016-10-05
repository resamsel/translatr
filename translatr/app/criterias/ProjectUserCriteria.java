package criterias;

import forms.SearchForm;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 5 Oct 2016
 */
public class ProjectUserCriteria extends AbstractSearchCriteria<ProjectUserCriteria>
{
	public static ProjectUserCriteria from(SearchForm search)
	{
		return new ProjectUserCriteria().with(search);
	}
}
