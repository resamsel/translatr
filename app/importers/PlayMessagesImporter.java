package importers;

import services.KeyService;
import services.MessageService;

import javax.inject.Inject;

public class PlayMessagesImporter extends PropertiesImporter
{
	@Inject
	public PlayMessagesImporter(KeyService keyService, MessageService messageService)
	{
		super(keyService, messageService);
	}
}
