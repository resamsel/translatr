package exporters;

import models.Locale;

public class PlayMessagesExporter extends PropertiesExporter
{
	private static final String FILENAME_DEFAULT = "messages";

	private static final String FILENAME_FORMAT = "messages.%s";

	@Override
	public String getFilename(Locale locale)
	{
		if(DEFAULT.equals(locale.name))
			return FILENAME_DEFAULT;
		return String.format(FILENAME_FORMAT, locale.name);
	}
}
