package criterias;

import forms.SearchForm;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LogEntryCriteria extends AbstractSearchCriteria<LogEntryCriteria>
{
	public static LogEntryCriteria from(SearchForm form)
	{
		return new LogEntryCriteria().with(form);
	}
}
