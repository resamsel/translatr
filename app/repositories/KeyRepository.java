package repositories;

import com.google.common.collect.ImmutableMap;
import com.google.inject.ImplementedBy;
import criterias.KeyCriteria;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.Key;
import models.Project;
import repositories.impl.KeyRepositoryImpl;

@ImplementedBy(KeyRepositoryImpl.class)
public interface KeyRepository extends ModelRepository<Key, UUID, KeyCriteria> {
  Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of("project", Arrays.asList("project", "project.owner"), "messages",
          Arrays.asList("messages", "messages.locale"));

  String[] PROPERTIES_TO_FETCH = {"project"};

  List<Key> latest(Project project, int limit);

  Key byProjectAndName(Project project, String name);

  Key byOwnerAndProjectAndName(String username, String projectName, String keyName,
                               String... fetches);
}
