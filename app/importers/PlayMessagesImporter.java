package importers;

import javax.inject.Inject;
import services.KeyService;
import services.MessageService;

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
