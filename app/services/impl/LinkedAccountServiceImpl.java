package services.impl;

import criterias.LinkedAccountCriteria;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.AccessToken;
import models.LinkedAccount;
import repositories.LinkedAccountRepository;
import services.CacheService;
import services.LinkedAccountService;
import services.LogEntryService;

/**
 * @author resamsel
 * @version 2 Oct 2016
 */
@Singleton
public class LinkedAccountServiceImpl
    extends AbstractModelService<LinkedAccount, Long, LinkedAccountCriteria>
    implements LinkedAccountService {

  @Inject
  public LinkedAccountServiceImpl(Validator validator, CacheService cache,
      LinkedAccountRepository linkedAccountRepository, LogEntryService logEntryService) {
    super(validator, cache, linkedAccountRepository, LinkedAccount::getCacheKey, logEntryService);
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
  protected void postSave(LinkedAccount t) {
    super.postSave(t);

    // When linked account has been created
    cache.removeByPrefix("linkedAccount:criteria:");
  }

  @Override
  protected void postUpdate(LinkedAccount t) {
    super.postUpdate(t);

    // When linked account has been updated, the linked account cache needs to be invalidated
    cache.removeByPrefix("linkedAccount:criteria:" + t.user.id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(LinkedAccount t) {
    super.postDelete(t);

    // When linked account has been deleted, the linked account cache needs to be invalidated
    cache.removeByPrefix("linkedAccount:criteria:");
  }
}
