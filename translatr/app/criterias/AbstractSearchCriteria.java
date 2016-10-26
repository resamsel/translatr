package criterias;

import java.util.UUID;

import com.avaje.ebean.ExpressionList;

import forms.SearchForm;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 31 Aug 2016
 */
public abstract class AbstractSearchCriteria<T extends AbstractSearchCriteria<T>> implements SearchCriteria
{
	private T self;

	private Integer offset;

	private Integer limit;

	private String order;

	private Boolean missing;

	private String search;

	private UUID userId;

	private UUID projectId;

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public AbstractSearchCriteria()
	{
		this.self = (T)this;
	}

	public UUID getUserId()
	{
		return userId;
	}

	public void setUserId(UUID userId)
	{
		this.userId = userId;
	}

	public T withUserId(UUID userId)
	{
		setUserId(userId);
		return self;
	}

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
	public T withProjectId(UUID projectId)
	{
		setProjectId(projectId);
		return self;
	}

	/**
	 * @return the limit
	 */
	public Integer getLimit()
	{
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(Integer limit)
	{
		this.limit = limit;
	}

	/**
	 * @param limit
	 * @return
	 */
	public T withLimit(Integer limit)
	{
		setLimit(limit);
		return self;
	}

	/**
	 * @return the order
	 */
	public String getOrder()
	{
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(String order)
	{
		this.order = order;
	}

	/**
	 * @param order
	 * @return
	 */
	public T withOrder(String order)
	{
		setOrder(order);
		return self;
	}

	/**
	 * @return the offset
	 */
	public Integer getOffset()
	{
		return offset;
	}

	/**
	 * @param offset the offset to set
	 */
	public void setOffset(Integer offset)
	{
		this.offset = offset;
	}

	/**
	 * @param offset
	 * @return
	 */
	public T withOffset(Integer offset)
	{
		setOffset(offset);
		return self;
	}

	/**
	 * @return the search
	 */
	public String getSearch()
	{
		return search;
	}

	/**
	 * @param search the search to set
	 */
	public void setSearch(String search)
	{
		this.search = search;
	}

	/**
	 * @param search
	 * @return
	 */
	public T withSearch(String search)
	{
		setSearch(search);
		return self;
	}

	public Boolean getMissing()
	{
		return missing;
	}

	public void setMissing(Boolean missing)
	{
		this.missing = missing;
	}

	public T withMissing(Boolean missing)
	{
		setMissing(missing);
		return self;
	}

	public T with(SearchForm form)
	{
		return self
			.withSearch(form.search)
			.withMissing(form.missing)
			.withOffset(form.offset)
			.withLimit(form.limit)
			.withOrder(form.order);
	}

	/**
	 * @param query
	 */
	public <U> ExpressionList<U> paging(ExpressionList<U> query)
	{
		if(getLimit() != null)
			query.setMaxRows(getLimit() + 1);

		if(getOffset() != null)
			query.setFirstRow(getOffset());

		if(getOrder() != null)
			query.order(getOrder());

		return query;
	}
}
