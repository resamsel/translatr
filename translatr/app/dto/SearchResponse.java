package dto;

import java.util.List;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 16 Sep 2016
 */
public class SearchResponse
{
	public List<Suggestion> suggestions;

	/**
	 * @param results
	 */
	private SearchResponse(List<Suggestion> suggestions)
	{
		this.suggestions = suggestions;
	}

	/**
	 * @param from
	 * @return
	 */
	public static SearchResponse from(List<Suggestion> suggestions)
	{
		return new SearchResponse(suggestions);
	}
}
