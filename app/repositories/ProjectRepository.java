package repositories;

import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableMap;
import com.google.inject.ImplementedBy;
import criterias.ProjectCriteria;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import models.Project;
import repositories.impl.ProjectRepositoryImpl;

@ImplementedBy(ProjectRepositoryImpl.class)
public interface ProjectRepository extends ModelRepository<Project, UUID, ProjectCriteria> {

  String FETCH_MEMBERS = "members";
  String FETCH_OWNER = "owner";
  String FETCH_LOCALES = "locales";
  String FETCH_KEYS = "keys";

  String[] PROPERTIES_TO_FETCH = {FETCH_OWNER, FETCH_MEMBERS};

  Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of(FETCH_MEMBERS, asList(FETCH_MEMBERS, FETCH_MEMBERS + ".user"));

  Project byOwnerAndName(String username, String name, String... fetches);
}
