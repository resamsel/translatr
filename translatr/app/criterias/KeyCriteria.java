package criterias;

import java.util.Collection;
import java.util.UUID;

import forms.SearchForm;

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

	private UUID localeId;

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

	/**
	 * @param search2
	 * @return
	 */
	public static KeyCriteria from(SearchForm form)
	{
		return new KeyCriteria().with(form);
	}
}
