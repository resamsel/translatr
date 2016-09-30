package utils;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 30 Sep 2016
 */
public enum SessionKey
{
	UserId("userId");

	private String key;

	SessionKey(String key)
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
