package repositories.impl;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import criterias.HasNextPagedList;
import criterias.LinkedAccountCriteria;
import javax.inject.Inject;
import javax.validation.Validator;
import models.LinkedAccount;
import play.cache.CacheApi;
import repositories.LinkedAccountRepository;
import repositories.LogEntryRepository;
import utils.QueryUtils;

public class LinkedAccountRepositoryImpl extends
    AbstractModelRepository<LinkedAccount, Long, LinkedAccountCriteria> implements
    LinkedAccountRepository {

  public final Find<Long, LinkedAccount> find = new Find<Long, LinkedAccount>() {
  };

  @Inject
  public LinkedAccountRepositoryImpl(Validator validator, CacheApi cache,
      LogEntryRepository logEntryRepository) {
    super(validator, cache, logEntryRepository);
  }

  @Override
  public PagedList<LinkedAccount> findBy(LinkedAccountCriteria criteria) {
    ExpressionList<LinkedAccount> query = QueryUtils.fetch(find.query(), PROPERTIES_TO_FETCH)
        .where();

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (criteria.getOrder() != null) {
      query.order(criteria.getOrder());
    } else {
      query.order("whenCreated");
    }

    criteria.paged(query);

    return HasNextPagedList.create(query);
  }

  @Override
  public LinkedAccount byId(Long id, String... fetches) {
    return find.setId(id).findUnique();
  }
}
