package importers;

import services.KeyService;
import services.MessageService;

import javax.inject.Inject;

public class PlayMessagesImporter extends PropertiesImporter
{
	/**
	 * @param keyService
	 * @param messageService
	 */
	@Inject
	public PlayMessagesImporter(KeyService keyService, MessageService messageService)
	{
		super(keyService, messageService);
	}
}
