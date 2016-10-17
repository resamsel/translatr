package converters;

import java.util.List;
import java.util.function.Function;

import models.Aggregate;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 17 Oct 2016
 */
public class ActivityCsvConverter implements Function<List<Aggregate>, String>
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String apply(List<Aggregate> t)
	{
		double max = Math.log(t.stream().mapToInt(a -> a.value).reduce(0, Math::max));

		return "Date,Value,RelativeValue\n" + t
			.stream()
			.map(a -> String.format("%s,%d,%.2f\n", a.date.toString("yyyy-MM-dd"), a.value, Math.log(a.value) / max))
			.reduce("", (a, b) -> a.concat(b));
	}
}
