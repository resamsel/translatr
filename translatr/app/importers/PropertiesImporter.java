package importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import criterias.KeyCriteria;
import models.Key;
import models.Locale;
import models.Message;
import services.KeyService;
import services.MessageService;
import utils.TransactionUtils;

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
	 * Map of key.name -> key
	 */
	private Map<String, Key> keys;

	/**
	 * Map of key.name -> message
	 */
	private Map<String, Message> messages;

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
		keys = Key
			.findBy(new KeyCriteria().withProjectId(locale.project.id).withNames(properties.stringPropertyNames()))
			.stream()
			.collect(Collectors.groupingBy(k -> k.name, Collectors.reducing(null, (a) -> a, (a, b) -> b)));
		messages = Message.byLocale(locale.id).stream().collect(
			Collectors.groupingBy(m -> m.key.name, Collectors.reducing(null, (a) -> a, (a, b) -> b)));

		TransactionUtils.batchExecute((tx) -> {
			for(String property : properties.stringPropertyNames())
				saveMessage(locale, property, (String)properties.get(property));
		});

		LOGGER.debug("Imported from file {}", file.getName());
	}

	private Message saveMessage(Locale locale, String keyName, String value)
	{
		LOGGER.debug("Key name: {}, value: {}", keyName, value);

		if(keyName == null || value == null || "".equals(value))
			return null;

		if(!keys.containsKey(keyName))
			keys.put(keyName, keyService.save(new Key(locale.project, keyName)));
		Key key = keys.get(keyName);

		if(!messages.containsKey(keyName))
			messages.put(keyName, new Message(locale, key, null));
		Message message = messages.get(keyName);

		if(!value.equals(message.value))
		{
			// Only update value when it has changed
			message.value = value;
			messageService.save(message);
		}

		return message;
	}
}
