package forms;

import java.util.UUID;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 4 Sep 2016
 */
public class SearchForm
{
	public String search;

	public UUID missingLocaleId;

	public Integer limit = 100;

	public Integer offset = 0;

	public String getSearch()
	{
		return search;
	}

	public void setSearch(String search)
	{
		this.search = search;
	}

	public UUID getMissingLocaleId()
	{
		return missingLocaleId;
	}

	public void setMissingLocaleId(UUID missingLocaleId)
	{
		this.missingLocaleId = missingLocaleId;
	}

	public Integer getLimit()
	{
		return limit;
	}

	public void setLimit(Integer limit)
	{
		this.limit = limit;
	}

	public Integer getOffset()
	{
		return offset;
	}

	public void setOffset(Integer offset)
	{
		this.offset = offset;
	}

	@Override
	public String toString()
	{
		return String.format("SearchForm [search=%s, limit=%s, offset=%s]", search, limit, offset);
	}
}
