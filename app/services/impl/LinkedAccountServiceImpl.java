package services.impl;

import io.ebean.PagedList;
import criterias.LinkedAccountCriteria;
import models.ActionType;
import models.LinkedAccount;
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
                                  AuthProvider authProvider, MetricService metricService) {
    super(validator, cache, linkedAccountRepository, LinkedAccount::getCacheKey, logEntryService, authProvider);
    this.metricService = metricService;
  }

  @Override
  protected PagedList<LinkedAccount> postFind(PagedList<LinkedAccount> pagedList) {
    metricService.logEvent(LinkedAccount.class, ActionType.Read);

    return super.postFind(pagedList);
  }

  @Override
  protected LinkedAccount postGet(LinkedAccount model) {
    metricService.logEvent(LinkedAccount.class, ActionType.Read);

    return super.postGet(model);
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
  protected void postCreate(LinkedAccount t) {
    super.postCreate(t);

    metricService.logEvent(LinkedAccount.class, ActionType.Create);

    // When linked account has been created
    cache.removeByPrefix("linkedAccount:criteria:");
  }

  @Override
  protected LinkedAccount postUpdate(LinkedAccount t) {
    super.postUpdate(t);

    metricService.logEvent(LinkedAccount.class, ActionType.Update);

    // When linked account has been updated, the linked account cache needs to be invalidated
    cache.removeByPrefix("linkedAccount:criteria:" + t.user.id);

    return t;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(LinkedAccount t) {
    super.postDelete(t);

    metricService.logEvent(LinkedAccount.class, ActionType.Delete);

    // When linked account has been deleted, the linked account cache needs to be invalidated
    cache.removeByPrefix("linkedAccount:criteria:");
  }
}
