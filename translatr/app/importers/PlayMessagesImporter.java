package importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.Key;
import models.Locale;
import models.Message;
import services.KeyService;
import services.MessageService;

public class PlayImporter implements Importer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayImporter.class);

	private final KeyService keyService;

	private final MessageService messageService;

	/**
	 * 
	 */
	@Inject
	public PlayImporter(KeyService keyService, MessageService messageService)
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

		if(keyName == null || value == null)
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
