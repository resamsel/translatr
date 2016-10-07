package utils;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 30 Sep 2016
 */
public enum ConfigKey
{
	/* The default user ID, will be removed in future releases */
	UserId("translatr.userId");

	private String key;

	ConfigKey(String key)
	{
		this.key = key;
	}

	public String key()
	{
		return key;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return key;
	}
}
