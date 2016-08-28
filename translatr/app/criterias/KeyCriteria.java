package criterias;

import java.util.UUID;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class KeyCriteria implements SearchCriteria
{
	private UUID projectId;

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
	public KeyCriteria withProjectId(UUID projectId)
	{
		setProjectId(projectId);
		return this;
	}
}
