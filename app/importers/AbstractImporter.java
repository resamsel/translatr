package importers;

import criterias.KeyCriteria;
import criterias.MessageCriteria;
import models.Key;
import models.Locale;
import models.Message;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.KeyService;
import services.MessageService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.stream.Collectors.toMap;

/**
 * @author resamsel
 * @version 7 Oct 2016
 */
public abstract class AbstractImporter implements Importer {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImporter.class);

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

  public AbstractImporter(KeyService keyService, MessageService messageService) {
    this.keyService = keyService;
    this.messageService = messageService;
  }

  @Override
  public void apply(File file, Locale locale) throws Exception {
    LOGGER.debug("Importing from file {}", file.getName());

    Properties properties = retrieveProperties(file, locale);

    load(locale, properties.stringPropertyNames());

    saveKeys(locale, properties);
    saveMessages(locale, properties);

    LOGGER.debug("Imported from file {}", file.getName());
  }

  protected abstract Properties retrieveProperties(File file, Locale locale) throws Exception;

  protected void load(Locale locale, Collection<String> keyNames) {
    keys = keyService.findBy(
        new KeyCriteria()
            .withLimit(Integer.MAX_VALUE)
            .withProjectId(locale.project.id)
            .withNames(keyNames))
        .getList()
        .stream()
        .collect(toMap(k -> k.name, a -> a));
    messages = messageService.findBy(
        new MessageCriteria()
            .withLimit(Integer.MAX_VALUE)
            .withLocaleId(locale.id))
        .getList()
        .stream()
        .collect(toMap(m -> m.key.name, a -> a));
  }

  void saveKeys(Locale locale, Properties properties) {
    List<Key> newKeys = new ArrayList<>();
    for (String keyName : properties.stringPropertyNames()) {
      String value = (String) properties.get(keyName);

      if (StringUtils.isEmpty(value)) {
        continue;
      }

      if (!keys.containsKey(keyName)) {
        newKeys.add(new Key(locale.project, keyName));
      }
    }

    // Update keys cache
    for (Key key : keyService.save(newKeys)) {
      keys.put(key.name, key);
    }
  }

  void saveMessages(Locale locale, Properties properties) {
    List<Message> newMessages = new ArrayList<>();
    for (String keyName : properties.stringPropertyNames()) {
      String value = (String) properties.get(keyName);

      if (StringUtils.isEmpty(value)) {
        continue;
      }

      if (!keys.containsKey(keyName))
      // Must not happen, keys have been created earlier
      {
        continue;
      }

      Key key = keys.get(keyName);

      if (!messages.containsKey(keyName)) {
        newMessages.add(new Message(locale, key, value));
        continue;
      }

      Message message = messages.get(keyName);
      if (!value.equals(message.value)) {
        // Only update value when it has changed
        message.value = value;
        newMessages.add(message);
      }
    }

    messageService.save(newMessages);
  }
}
