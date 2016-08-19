package criterias;

import java.util.UUID;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 19 Aug 2016
 */
public class LocaleCriteria implements SearchCriteria
{
	private UUID projectId;

	private String localeName;

	public UUID getProjectId()
	{
		return projectId;
	}

	public void setProjectId(UUID projectId)
	{
		this.projectId = projectId;
	}

    /**
     * @param projectId the projectId to set
     * @return this
     */
    public LocaleCriteria withProjectId(UUID projectId) {
        setProjectId(projectId);
        return this;
    }

	public String getLocaleName()
	{
		return localeName;
	}

	public void setLocaleName(String localeName)
	{
		this.localeName = localeName;
	}

    /**
     * @param localeName the localeName to set
     * @return this
     */
    public LocaleCriteria withLocaleName(String localeName) {
        setLocaleName(localeName);
        return this;
    }
}
