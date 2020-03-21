package importers;

import com.gettextresourcebundle.GettextResourceBundle;
import models.Locale;
import services.KeyService;
import services.MessageService;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 *
 * @author resamsel
 * @version 7 Oct 2016
 */
public class GettextImporter extends AbstractImporter implements Importer
{
	@Inject
	public GettextImporter(KeyService keyService, MessageService messageService)
	{
		super(keyService, messageService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Properties retrieveProperties(InputStream inputStream, Locale locale)
	{
		GettextResourceBundle bundle =
					new GettextResourceBundle(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

		return convert(bundle);
	}

	private static Properties convert(ResourceBundle resource)
	{
		Properties properties = new Properties();

		Enumeration<String> keys = resource.getKeys();
		while(keys.hasMoreElements())
		{
			String key = keys.nextElement();
			properties.put(key, resource.getString(key));
		}

		return properties;
	}
}
