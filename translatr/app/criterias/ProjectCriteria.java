package criterias;

import java.util.UUID;

import forms.SearchForm;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 26 Sep 2016
 */
public class ProjectCriteria extends AbstractSearchCriteria<ProjectCriteria>
{
	private UUID ownerId;

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

	public static ProjectCriteria from(SearchForm search)
	{
		return new ProjectCriteria()
			.withSearch(search.search)
			.withMissing(search.missing)
			.withOffset(search.offset)
			.withLimit(search.limit)
			.withOrder(search.order);
	}
}
