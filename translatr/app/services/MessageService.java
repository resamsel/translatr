package services;

import com.google.inject.ImplementedBy;

import models.Message;
import models.Project;
import services.impl.MessageServiceImpl;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(MessageServiceImpl.class)
public interface MessageService extends ModelService<Message>
{
	/**
	 * @param project
	 * @return
	 */
	int countBy(Project project);
}
