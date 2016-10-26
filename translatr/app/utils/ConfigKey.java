package utils;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 30 Sep 2016
 */
public enum ConfigKey
{
	/* The list of available auth providers */
	AuthProviders("translatr.auth.providers");

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
