package repositories.impl;

import static models.ProjectUser.FETCH_PROJECT;
import static utils.Stopwatch.log;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.google.common.collect.ImmutableMap;
import criterias.HasNextPagedList;
import criterias.ProjectUserCriteria;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.ProjectUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.CacheApi;
import repositories.LogEntryRepository;
import repositories.ProjectUserRepository;
import utils.QueryUtils;

@Singleton
public class ProjectUserRepositoryImpl extends
    AbstractModelRepository<ProjectUser, Long, ProjectUserCriteria> implements
    ProjectUserRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectUserRepositoryImpl.class);

  private static final List<String> PROPERTIES_TO_FETCH = Collections.singletonList(FETCH_PROJECT);

  private static final Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of(FETCH_PROJECT, Arrays.asList(FETCH_PROJECT, FETCH_PROJECT + ".owner"));

  private final Find<Long, ProjectUser> find = new Find<Long, ProjectUser>() {
  };

  @Inject
  public ProjectUserRepositoryImpl(Validator validator, CacheApi cache,
      LogEntryRepository logEntryRepository) {
    super(validator, cache, logEntryRepository);
  }

  @Override
  public PagedList<ProjectUser> findBy(ProjectUserCriteria criteria) {
    return log(() -> HasNextPagedList.create(findQuery(criteria).query().fetch("user")), LOGGER,
        "findBy");
  }

  @Override
  public ProjectUser byId(Long id, String... propertiesToFetch) {
    return QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH, FETCH_MAP).where().idEq(id)
        .findUnique();
  }

  @Override
  public int countBy(ProjectUserCriteria criteria) {
    return log(() -> findQuery(criteria).findCount(), LOGGER, "countBy");
  }

  private ExpressionList<ProjectUser> findQuery(ProjectUserCriteria criteria) {
    ExpressionList<ProjectUser> query = find.where();

    query.eq("project.deleted", false);

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    criteria.paged(query);

    return query;
  }
}
