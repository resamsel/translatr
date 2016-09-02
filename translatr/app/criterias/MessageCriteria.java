package criterias;

import java.util.UUID;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class MessageCriteria extends AbstractSearchCriteria<MessageCriteria>
{
	private UUID localeId;

	private String keyName;

	/**
	 * @return the keyName
	 */
	public String getKeyName()
	{
		return keyName;
	}

	/**
	 * @param keyName the keyName to set
	 */
	public void setKeyName(String keyName)
	{
		this.keyName = keyName;
	}

	/**
	 * @param keyName the keyName to set
	 * @return this
	 */
	public MessageCriteria withKeyName(String keyName)
	{
		setKeyName(keyName);
		return this;
	}

	/**
	 * @return the localeId
	 */
	public UUID getLocaleId()
	{
		return localeId;
	}

	/**
	 * @param localeId the localeId to set
	 */
	public void setLocaleId(UUID localeId)
	{
		this.localeId = localeId;
	}

	/**
	 * @param localeId
	 * @return
	 */
	public MessageCriteria withLocaleId(UUID localeId)
	{
		setLocaleId(localeId);
		return this;
	}
}
