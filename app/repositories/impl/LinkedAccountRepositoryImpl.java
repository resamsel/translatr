package repositories.impl;

import actors.ActivityActorRef;
import criterias.LinkedAccountCriteria;
import criterias.PagedListFactory;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import models.LinkedAccount;
import repositories.LinkedAccountRepository;
import repositories.Persistence;
import services.AuthProvider;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

@Singleton
public class LinkedAccountRepositoryImpl extends
    AbstractModelRepository<LinkedAccount, Long, LinkedAccountCriteria> implements
    LinkedAccountRepository {

  @Inject
  public LinkedAccountRepositoryImpl(Persistence persistence,
                                     Validator validator,
                                     AuthProvider authProvider,
                                     ActivityActorRef activityActor) {
    super(persistence, validator, authProvider, activityActor);
  }

  @Override
  public PagedList<LinkedAccount> findBy(LinkedAccountCriteria criteria) {
    ExpressionList<LinkedAccount> query = QueryUtils.fetch(persistence.find(LinkedAccount.class), PROPERTIES_TO_FETCH)
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

    return PagedListFactory.create(query);
  }

  @Override
  public LinkedAccount byId(Long id, String... fetches) {
    return persistence.find(LinkedAccount.class)
            .setId(id)
            .findOneOrEmpty()
            .orElse(null);
  }
}
