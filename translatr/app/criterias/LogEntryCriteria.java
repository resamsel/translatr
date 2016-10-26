package criterias;

import forms.SearchForm;

/**
 * 
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
