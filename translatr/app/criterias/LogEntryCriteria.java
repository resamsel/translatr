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

	public static LogEntryCriteria from(SearchForm search)
	{
		return new LogEntryCriteria()
			.withSearch(search.search)
			.withMissing(search.missing)
			.withOffset(search.offset)
			.withLimit(search.limit)
			.withOrder(search.order);
	}
}
