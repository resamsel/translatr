package criterias;

import java.util.Collection;
import java.util.UUID;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class KeyCriteria extends AbstractSearchCriteria<KeyCriteria>
{
	private Collection<String> names;

	private String search;

	private UUID localeId;

	private Boolean missing;

	/**
	 * @return the names
	 */
	public Collection<String> getNames()
	{
		return names;
	}

	/**
	 * @param names the names to set
	 */
	public void setNames(Collection<String> names)
	{
		this.names = names;
	}

	/**
	 * @param names
	 * @return
	 */
	public KeyCriteria withNames(Collection<String> names)
	{
		setNames(names);
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
	public KeyCriteria withSearch(String search)
	{
		setSearch(search);
		return this;
	}

	/**
	 * @return the untranslated
	 */
	public UUID getLocaleId()
	{
		return localeId;
	}

	/**
	 * @param localeId the untranslated to set
	 */
	public void setLocaleId(UUID localeId)
	{
		this.localeId = localeId;
	}

	/**
	 * @param localeId
	 * @return
	 */
	public KeyCriteria withLocaleId(UUID localeId)
	{
		setLocaleId(localeId);
		return this;
	}

	public Boolean getMissing()
	{
		return missing;
	}

	public void setMissing(Boolean missing)
	{
		this.missing = missing;
	}

	public KeyCriteria withMissing(Boolean missing)
	{
		setMissing(missing);
		return this;
	}
}
