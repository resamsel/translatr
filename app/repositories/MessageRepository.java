package repositories;

import com.google.inject.ImplementedBy;
import criterias.MessageCriteria;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import models.Key;
import models.Message;
import models.Project;
import repositories.impl.MessageRepositoryImpl;

@ImplementedBy(MessageRepositoryImpl.class)
public interface MessageRepository extends ModelRepository<Message, UUID, MessageCriteria> {

  Map<UUID, Message> byIds(List<UUID> ids);

  int countBy(Project project);

  List<Message> byLocale(UUID localeId);

  List<Message> byLocales(Collection<UUID> ids);

  List<Message> byKey(Key key);

  List<Message> byKeys(Collection<UUID> ids);

  List<Message> latest(Project project, int limit);
}
