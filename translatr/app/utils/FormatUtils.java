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
		java.util.Locale l = java.util.Locale.forLanguageTag(locale.name.replaceAll("_", "-"));

		if(l.getDisplayName().equals(locale.name))
			return locale.name;

		return String.format("%s (%s)", l.getDisplayName(), locale.name);
	}
}
