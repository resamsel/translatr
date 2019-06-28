package importers;

import services.KeyService;
import services.MessageService;

import javax.inject.Inject;

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
