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
public class LinkedAccountCriteria extends AbstractSearchCriteria<LinkedAccountCriteria>
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

	public LinkedAccountCriteria withUserId(UUID userId)
	{
		setUserId(userId);
		return this;
	}

	public static LinkedAccountCriteria from(SearchForm form)
	{
		return new LinkedAccountCriteria().with(form);
	}
}
