package services.impl;

import com.avaje.ebean.PagedList;
import criterias.LinkedAccountCriteria;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.LinkedAccount;
import play.cache.CacheApi;
import repositories.LinkedAccountRepository;
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
  public LinkedAccountServiceImpl(Validator validator, CacheApi cache,
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
}
