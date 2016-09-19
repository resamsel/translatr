package exporters;

import models.Locale;

public class JavaPropertiesExporter extends PropertiesExporter
{
	private static final String FILENAME_DEFAULT = "messages.properties";

	private static final String FILENAME_FORMAT = "messages_%s.properties";

	@Override
	public String getFilename(Locale locale)
	{
		if(DEFAULT.equals(locale.name))
			return FILENAME_DEFAULT;
		return String.format(FILENAME_FORMAT, locale.name);
	}
}
