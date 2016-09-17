package dto;

import java.util.List;
import java.util.stream.Collectors;

import models.Suggestable;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 16 Sep 2016
 */
public class Suggestion
{
	public String value;

	public Suggestable.Data data;

	public static Suggestion from(Suggestable suggestable)
	{
		Suggestion out = new Suggestion();

		out.value = suggestable.value();
		out.data = suggestable.data();

		return out;
	}

	public static List<Suggestion> from(List<? extends Suggestable> suggestables)
	{
		return suggestables.stream().map(s -> from(s)).collect(Collectors.toList());
	}
}
