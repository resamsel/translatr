package repositories;

import com.google.common.collect.ImmutableMap;
import com.google.inject.ImplementedBy;
import criterias.MessageCriteria;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import models.Key;
import models.Message;
import models.Project;
import repositories.impl.MessageRepositoryImpl;

@ImplementedBy(MessageRepositoryImpl.class)
public interface MessageRepository extends ModelRepository<Message, UUID, MessageCriteria> {

  String FETCH_LOCALE = "locale";
  String FETCH_KEY = "key";

  String[] PROPERTIES_TO_FETCH = {FETCH_LOCALE, FETCH_KEY};

  Map<String, List<String>> FETCH_MAP = ImmutableMap.of(FETCH_LOCALE,
      Arrays.asList(FETCH_LOCALE, FETCH_LOCALE + ".project", FETCH_LOCALE + ".project.owner"),
      FETCH_KEY, Collections.singletonList(FETCH_KEY));

  Map<UUID, Message> byIds(List<UUID> ids);

  int countBy(Project project);

  List<Message> byLocale(UUID localeId);

  List<Message> byLocales(Collection<UUID> ids);

  List<Message> byKey(Key key);

  List<Message> byKeys(Collection<UUID> ids);

  List<Message> latest(Project project, int limit);
}
