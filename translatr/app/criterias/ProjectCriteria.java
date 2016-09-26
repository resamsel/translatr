package criterias;

import forms.SearchForm;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 26 Sep 2016
 */
public class ProjectCriteria extends AbstractSearchCriteria<ProjectCriteria>
{
	public static ProjectCriteria from(SearchForm search)
	{
		return new ProjectCriteria()
			.withSearch(search.search)
			.withMissing(search.missing)
			.withOffset(search.offset)
			.withLimit(search.limit)
			.withOrder(search.order);
	}
}
