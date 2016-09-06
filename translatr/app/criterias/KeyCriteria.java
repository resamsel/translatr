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

	private UUID untranslated;

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
	public UUID getUntranslated()
	{
		return untranslated;
	}

	/**
	 * @param untranslated the untranslated to set
	 */
	public void setUntranslated(UUID untranslated)
	{
		this.untranslated = untranslated;
	}

	/**
	 * @param untranslated
	 * @return
	 */
	public KeyCriteria withUntranslated(UUID untranslated)
	{
		setUntranslated(untranslated);
		return this;
	}
}
