package services.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import models.LinkedAccount;
import play.Configuration;
import services.LinkedAccountService;
import services.LogEntryService;

/**
 *
 * @author resamsel
 * @version 2 Oct 2016
 */
@Singleton
public class LinkedAccountServiceImpl extends AbstractModelService<LinkedAccount, Long>
    implements LinkedAccountService {
  /**
   * @param configuration
   */
  @Inject
  public LinkedAccountServiceImpl(Configuration configuration, Validator validator,
      LogEntryService logEntryService) {
    super(configuration, validator, logEntryService);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected LinkedAccount byId(Long id) {
    return LinkedAccount.byId(id);
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
