package services.impl;

import actors.ActivityActorRef;
import io.ebean.PagedList;
import criterias.LinkedAccountCriteria;
import models.ActionType;
import models.LinkedAccount;
import play.mvc.Http;
import repositories.LinkedAccountRepository;
import services.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

/**
 * @author resamsel
 * @version 2 Oct 2016
 */
@Singleton
public class LinkedAccountServiceImpl
        extends AbstractModelService<LinkedAccount, Long, LinkedAccountCriteria>
        implements LinkedAccountService {

  private final MetricService metricService;

  @Inject
  public LinkedAccountServiceImpl(Validator validator, CacheService cache,
                                  LinkedAccountRepository linkedAccountRepository, LogEntryService logEntryService,
                                  AuthProvider authProvider, MetricService metricService, ActivityActorRef activityActor) {
    super(validator, cache, linkedAccountRepository, LinkedAccount::getCacheKey, logEntryService, authProvider, activityActor);
    this.metricService = metricService;
  }

  @Override
  protected PagedList<LinkedAccount> postFind(PagedList<LinkedAccount> pagedList, Http.Request request) {
    metricService.logEvent(LinkedAccount.class, ActionType.Read);

    return super.postFind(pagedList, request);
  }

  @Override
  protected LinkedAccount postGet(LinkedAccount model, Http.Request request) {
    metricService.logEvent(LinkedAccount.class, ActionType.Read);

    return super.postGet(model, request);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LinkedAccount create(LinkedAccount linkedAccount) {
    final LinkedAccount ret = new LinkedAccount();

    ret.providerKey = linkedAccount.providerKey;
    ret.providerUserId = linkedAccount.providerUserId;

    return ret;
  }

  @Override
  protected void postCreate(LinkedAccount t, Http.Request request) {
    super.postCreate(t, request);

    metricService.logEvent(LinkedAccount.class, ActionType.Create);

    // When linked account has been created
    cache.removeByPrefix("linkedAccount:criteria:");
  }

  @Override
  protected LinkedAccount postUpdate(LinkedAccount t, Http.Request request) {
    super.postUpdate(t, request);

    metricService.logEvent(LinkedAccount.class, ActionType.Update);

    // When linked account has been updated, the linked account cache needs to be invalidated
    cache.removeByPrefix("linkedAccount:criteria:" + t.user.id);

    return t;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(LinkedAccount t, Http.Request request) {
    super.postDelete(t, request);

    metricService.logEvent(LinkedAccount.class, ActionType.Delete);

    // When linked account has been deleted, the linked account cache needs to be invalidated
    cache.removeByPrefix("linkedAccount:criteria:");
  }
}
