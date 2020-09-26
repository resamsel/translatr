package repositories.impl;

import criterias.AccessTokenCriteria;
import criterias.ContextCriteria;
import criterias.PagedListFactory;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
import models.AccessToken;
import org.apache.commons.lang3.StringUtils;
import repositories.AccessTokenRepository;
import repositories.Persistence;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Singleton
public class AccessTokenRepositoryImpl extends
        AbstractModelRepository<AccessToken, Long, AccessTokenCriteria> implements
        AccessTokenRepository {

  @Inject
  public AccessTokenRepositoryImpl(Persistence persistence, Validator validator) {
    super(persistence, validator);
  }

  @Override
  public PagedList<AccessToken> findBy(AccessTokenCriteria criteria) {
    ExpressionList<AccessToken> query = fetch(criteria.getFetches()).where();

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (StringUtils.isNoneEmpty(criteria.getSearch())) {
      query.ilike("name", "%" + criteria.getSearch() + "%");
    }

    criteria.paged(query);

    return PagedListFactory.create(query);
  }

  @Override
  public AccessToken byId(Long id, String... fetches) {
    return fetch(fetches).setId(id).findOne();
  }

  @Override
  public AccessToken byKey(String key) {
    return fetch().where().eq("key", key).findOne();
  }

  @Override
  public AccessToken byUserAndName(UUID userId, String name) {
    return persistence.find(AccessToken.class)
            .where()
            .eq("user.id", userId)
            .eq("name", name)
            .findOne();
  }

  @Override
  protected Query<AccessToken> createQuery(ContextCriteria criteria) {
    return fetch(criteria.getFetches());
  }

  private Query<AccessToken> fetch(List<String> fetches) {
    return createQuery(AccessToken.class, PROPERTIES_TO_FETCH, FETCH_MAP, fetches);
  }

  private Query<AccessToken> fetch(String... fetches) {
    return fetch(fetches != null ? Arrays.asList(fetches) : Collections.emptyList());
  }
}
