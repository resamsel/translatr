package repositories.impl;

import actors.ActivityActorRef;
import criterias.ContextCriteria;
import criterias.LinkedAccountCriteria;
import criterias.PagedListFactory;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
import models.LinkedAccount;
import repositories.LinkedAccountRepository;
import repositories.Persistence;
import services.AuthProvider;

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
  protected Query<LinkedAccount> createQuery(ContextCriteria criteria) {
    return createQuery(LinkedAccount.class, PROPERTIES_TO_FETCH, criteria.getFetches());
  }

  @Override
  public PagedList<LinkedAccount> findBy(LinkedAccountCriteria criteria) {
    ExpressionList<LinkedAccount> query = createQuery(criteria).where();

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
            .findOne();
  }
}
