package utils;

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import criterias.KeyCriteria;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import models.Key;
import models.Locale;
import models.Message;
import models.Project;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 28 Aug 2016
 */
public class StatUtils
{
	public static List<Stat> getMessageStats(Project project)
	{
		return Message
			.findBy(new MessageCriteria().withProjectId(project.id))
			.stream()
			.collect(Collectors.groupingBy(m -> withMinuteSecondMillisReset(m.whenUpdated)))
			.entrySet()
			.stream()
			.map(e -> new Stat(e.getKey().getMillis(), e.getValue().size()))
			.collect(Collectors.toList());
	}

	public static List<Stat> getKeyStats(Project project)
	{
		return Key
			.findBy(new KeyCriteria().withProjectId(project.id))
			.stream()
			.collect(Collectors.groupingBy(k -> withMinuteSecondMillisReset(k.whenUpdated)))
			.entrySet()
			.stream()
			.map(e -> new Stat(e.getKey().getMillis(), e.getValue().size()))
			.collect(Collectors.toList());
	}

	public static List<Stat> getLocaleStats(Project project)
	{
		return Locale
			.findBy(new LocaleCriteria().withProjectId(project.id))
			.stream()
			.collect(Collectors.groupingBy(l -> withMinuteSecondMillisReset(l.whenUpdated)))
			.entrySet()
			.stream()
			.map(e -> new Stat(e.getKey().getMillis(), e.getValue().size()))
			.collect(Collectors.toList());
	}

	public static long toMillisOfHour(DateTime dateTime)
	{
		return withMinuteSecondMillisReset(dateTime).getMillis();
	}

	public static DateTime withMinuteSecondMillisReset(DateTime dateTime)
	{
		return dateTime.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
	}
}
