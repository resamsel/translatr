package criterias;

import forms.SearchForm;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LinkedAccountCriteria extends AbstractSearchCriteria<LinkedAccountCriteria>
{
	public static LinkedAccountCriteria from(SearchForm form)
	{
		return new LinkedAccountCriteria().with(form);
	}
}
