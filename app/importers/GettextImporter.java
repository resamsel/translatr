package importers;

import com.gettextresourcebundle.GettextResourceBundle;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.inject.Inject;
import models.Locale;
import services.KeyService;
import services.MessageService;

/**
 *
 * @author resamsel
 * @version 7 Oct 2016
 */
public class GettextImporter extends AbstractImporter implements Importer
{
	/**
	 * @param keyService
	 * @param messageService
	 */
	@Inject
	public GettextImporter(KeyService keyService, MessageService messageService)
	{
		super(keyService, messageService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Properties retrieveProperties(File file, Locale locale) throws Exception
	{
		GettextResourceBundle bundle =
					new GettextResourceBundle(new InputStreamReader(new FileInputStream(file), "UTF-8"));

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
