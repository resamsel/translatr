package criterias;

import java.util.UUID;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 31 Aug 2016
 */
public abstract class AbstractSearchCriteria<T extends SearchCriteria> implements SearchCriteria
{
	private T self;

	private Integer offset;

	private Integer limit;

	private String order;

	private UUID projectId;

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public AbstractSearchCriteria()
	{
		this.self = (T)this;
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
}
