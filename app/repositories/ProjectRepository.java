package repositories;

import com.google.common.collect.ImmutableMap;
import com.google.inject.ImplementedBy;
import criterias.ProjectCriteria;
import models.Project;
import repositories.impl.ProjectRepositoryImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@ImplementedBy(ProjectRepositoryImpl.class)
public interface ProjectRepository extends ModelRepository<Project, UUID, ProjectCriteria> {

    String FETCH_MEMBERS = "members";
    String FETCH_OWNER = "owner";
    String FETCH_LOCALES = "locales";
    String FETCH_KEYS = "keys";

    String[] PROPERTIES_TO_FETCH = {FETCH_OWNER, FETCH_MEMBERS};

    Map<String, List<String>> FETCH_MAP = ImmutableMap
            .of(
                    FETCH_OWNER, singletonList(FETCH_OWNER),
                    FETCH_MEMBERS, asList(FETCH_MEMBERS, FETCH_MEMBERS + ".user"),
                    FETCH_KEYS, singletonList(FETCH_KEYS),
                    FETCH_LOCALES, singletonList(FETCH_LOCALES)
            );

    Project byOwnerAndName(String username, String name, String... fetches);
}
