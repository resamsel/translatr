package importers;

import services.KeyService;
import services.MessageService;

import javax.inject.Inject;

public class JavaPropertiesImporter extends PropertiesImporter
{
	@Inject
	public JavaPropertiesImporter(KeyService keyService, MessageService messageService)
	{
		super(keyService, messageService);
	}
}
