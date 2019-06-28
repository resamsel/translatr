package repositories;

import com.google.common.collect.ImmutableMap;
import com.google.inject.ImplementedBy;
import criterias.ProjectUserCriteria;
import models.ProjectUser;
import repositories.impl.ProjectUserRepositoryImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static models.ProjectUser.FETCH_PROJECT;

@ImplementedBy(ProjectUserRepositoryImpl.class)
public interface ProjectUserRepository extends
    ModelRepository<ProjectUser, Long, ProjectUserCriteria> {

  List<String> PROPERTIES_TO_FETCH = Collections.singletonList(FETCH_PROJECT);

  Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of(FETCH_PROJECT, Arrays.asList(FETCH_PROJECT, FETCH_PROJECT + ".owner"));

  int countBy(ProjectUserCriteria criteria);
}
