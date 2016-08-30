package importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.Key;
import models.Locale;
import models.Message;
import services.KeyService;
import services.MessageService;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 30 Aug 2016
 */
public abstract class PropertiesImporter implements Importer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesImporter.class);

	private final KeyService keyService;

	private final MessageService messageService;

	/**
	 * 
	 */
	protected PropertiesImporter(KeyService keyService, MessageService messageService)
	{
		this.keyService = keyService;
		this.messageService = messageService;
	}

	@Override
	public void apply(File file, Locale locale) throws Exception
	{
		LOGGER.debug("Importing from file {}", file.getName());

		Properties properties = new Properties();
		properties.load(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		for(String property : properties.stringPropertyNames())
			saveMessage(locale, property, (String)properties.get(property));

		LOGGER.debug("Imported from file {}", file.getName());
	}

	private Message saveMessage(Locale locale, String keyName, String value)
	{
		LOGGER.debug("Key name: {}, value: {}", keyName, value);

		if(keyName == null || value == null || "".equals(value))
			return null;

		Key key = Key.byProjectAndName(locale.project, keyName);
		if(key == null)
			key = keyService.save(new Key(locale.project, keyName));

		Message message = Message.byKeyAndLocale(key, locale);
		if(message == null)
			message = new Message(locale, key, value);
		else
			message.value = value;

		return messageService.save(message);
	}
}
