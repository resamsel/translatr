package criterias;

import forms.SearchForm;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LocaleCriteria extends AbstractSearchCriteria<LocaleCriteria>
{
	private String localeName;

	public String getLocaleName()
	{
		return localeName;
	}

	public void setLocaleName(String localeName)
	{
		this.localeName = localeName;
	}

	/**
	 * @param localeName the localeName to set
	 * @return this
	 */
	public LocaleCriteria withLocaleName(String localeName)
	{
		setLocaleName(localeName);
		return this;
	}

	public static LocaleCriteria from(SearchForm search)
	{
		return new LocaleCriteria()
			.withSearch(search.search)
			.withMissing(search.missing)
			.withOffset(search.offset)
			.withLimit(search.limit)
			.withOrder(search.order);
	}
}
