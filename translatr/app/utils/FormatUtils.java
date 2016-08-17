package utils;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

import models.Locale;

public class FormatUtils
{
	public static final String pretty(DateTime dateTime)
	{
		return new PrettyTime().format(dateTime.toDate());
	}

	public static final String formatLocale(Locale locale)
	{
		return formatLocale(locale.name);
	}

	public static final String formatLocale(String localeName)
	{
		java.util.Locale l = java.util.Locale.forLanguageTag(localeName.replaceAll("_", "-"));

		if(l.getDisplayName().equalsIgnoreCase(localeName))
			return localeName;

		return String.format("%s (%s)", l.getDisplayName(), localeName);
	}
}
