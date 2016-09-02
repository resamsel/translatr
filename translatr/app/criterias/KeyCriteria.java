package criterias;

import java.util.Collection;

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
}
