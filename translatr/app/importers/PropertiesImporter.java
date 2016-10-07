package importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.Locale;
import services.KeyService;
import services.MessageService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 30 Aug 2016
 */
public abstract class PropertiesImporter extends AbstractImporter implements Importer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesImporter.class);

	/**
	 * 
	 */
	protected PropertiesImporter(KeyService keyService, MessageService messageService)
	{
		super(keyService, messageService);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	@Override
	protected Properties retrieveProperties(File file, Locale locale) throws IOException
	{
		Properties properties = new Properties();

		properties.load(new InputStreamReader(new FileInputStream(file), "UTF-8"));

		return properties;
	}

	@Override
	public void apply(File file, Locale locale) throws Exception
	{
		LOGGER.debug("Importing from file {}", file.getName());

		Properties properties = new Properties();
		properties.load(new InputStreamReader(new FileInputStream(file), "UTF-8"));

		load(locale, properties.stringPropertyNames());

		saveKeys(locale, properties);
		saveMessages(locale, properties);

		LOGGER.debug("Imported from file {}", file.getName());
	}
}
