package criterias;

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

	private String search;

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

	/**
	 * @return the search
	 */
	public String getSearch()
	{
		return search;
	}

	/**
	 * @param search the search to set
	 */
	public void setSearch(String search)
	{
		this.search = search;
	}

	/**
	 * @param search
	 * @return
	 */
	public LocaleCriteria withSearch(String search)
	{
		setSearch(search);
		return this;
	}
}
