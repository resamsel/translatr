package criterias;

import java.util.UUID;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class MessageCriteria implements SearchCriteria
{
	private UUID projectId;

	private String keyName;

	/**
	 * @return the projectId
	 */
	public UUID getProjectId()
	{
		return projectId;
	}

	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(UUID projectId)
	{
		this.projectId = projectId;
	}

	/**
	 * @param projectId the projectId to set
	 * @return this
	 */
	public MessageCriteria withProjectId(UUID projectId)
	{
		setProjectId(projectId);
		return this;
	}

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
}
