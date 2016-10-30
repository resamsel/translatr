package criterias;

import java.util.UUID;

import forms.SearchForm;

/**
 *
 * @author resamsel
 * @version 26 Sep 2016
 */
public class ProjectCriteria extends AbstractSearchCriteria<ProjectCriteria>
{
	private UUID ownerId;

	private UUID memberId;

	public UUID getOwnerId()
	{
		return ownerId;
	}

	public void setOwnerId(UUID ownerId)
	{
		this.ownerId = ownerId;
	}

	public ProjectCriteria withOwnerId(UUID ownerId)
	{
		this.ownerId = ownerId;
		return this;
	}

	public UUID getMemberId()
	{
		return memberId;
	}

	public void setMemberId(UUID memberId)
	{
		this.memberId = memberId;
	}

	public ProjectCriteria withMemberId(UUID memberId)
	{
		this.memberId = memberId;
		return this;
	}

	public static ProjectCriteria from(SearchForm form)
	{
		return new ProjectCriteria().with(form);
	}
}
