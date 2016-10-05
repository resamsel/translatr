package criterias;

import java.util.UUID;

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
	private UUID userId;

	public UUID getUserId()
	{
		return userId;
	}

	public void setUserId(UUID userId)
	{
		this.userId = userId;
	}

	public LogEntryCriteria withUserId(UUID userId)
	{
		setUserId(userId);
		return this;
	}

	public static LogEntryCriteria from(SearchForm form)
	{
		return new LogEntryCriteria().with(form);
	}
}
