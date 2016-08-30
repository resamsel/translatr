package importers;

import javax.inject.Inject;

import services.KeyService;
import services.MessageService;

public class JavaPropertiesImporter extends PropertiesImporter
{
	/**
	 * @param keyService
	 * @param messageService
	 */
	@Inject
	public JavaPropertiesImporter(KeyService keyService, MessageService messageService)
	{
		super(keyService, messageService);
	}
}
