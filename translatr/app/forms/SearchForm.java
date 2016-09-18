package forms;

import play.Configuration;
import play.data.Form;
import play.data.FormFactory;

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

	public Boolean missing;

	public Integer limit;

	public Integer offset = 0;

	public String getSearch()
	{
		return search;
	}

	public void setSearch(String search)
	{
		this.search = search;
	}

	/**
	 * @return the missing
	 */
	public boolean isMissing()
	{
		return missing;
	}

	/**
	 * @param missing the missing to set
	 */
	public void setMissing(boolean missing)
	{
		this.missing = missing;
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

	public static Form<SearchForm> bindFromRequest(FormFactory formFactory, Configuration configuration)
	{
		Form<SearchForm> out = formFactory.form(SearchForm.class).bindFromRequest();

		SearchForm defaults = out.get();

		if(defaults.missing == null)
			defaults.missing = configuration.getBoolean("translatr.search.missing", false);
		if(defaults.limit == null)
			defaults.limit = configuration.getInt("translatr.search.limit", 20);

		return out;
	}
}
