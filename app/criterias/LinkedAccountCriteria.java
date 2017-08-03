package criterias;

import forms.SearchForm;

/**
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LinkedAccountCriteria extends AbstractSearchCriteria<LinkedAccountCriteria>
{

	public LinkedAccountCriteria() {
		super("linkedAccount");
	}

	public static LinkedAccountCriteria from(SearchForm form)
	{
		return new LinkedAccountCriteria().with(form);
	}
}
