package repositories.impl;

import actors.ActivityActor;
import akka.actor.ActorRef;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import criterias.LinkedAccountCriteria;
import criterias.PagedListFactory;
import models.LinkedAccount;
import repositories.LinkedAccountRepository;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Validator;

public class LinkedAccountRepositoryImpl extends
    AbstractModelRepository<LinkedAccount, Long, LinkedAccountCriteria> implements
    LinkedAccountRepository {

  public final Find<Long, LinkedAccount> find = new Find<Long, LinkedAccount>() {
  };

  @Inject
  public LinkedAccountRepositoryImpl(Validator validator,
      @Named(ActivityActor.NAME) ActorRef activityActor) {
    super(validator, activityActor);
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

    return PagedListFactory.create(query);
  }

  @Override
  public LinkedAccount byId(Long id, String... fetches) {
    return find.setId(id).findUnique();
  }
}
