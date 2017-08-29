package services.api;

import com.google.inject.ImplementedBy;
import criterias.MessageCriteria;
import dto.Message;
import java.util.UUID;
import services.api.impl.MessageApiServiceImpl;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@ImplementedBy(MessageApiServiceImpl.class)
public interface MessageApiService extends ApiService<Message, UUID, MessageCriteria> {
}
