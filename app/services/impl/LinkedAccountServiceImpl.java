package services.impl;

import com.avaje.ebean.PagedList;
import criterias.LinkedAccountCriteria;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.LinkedAccount;
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
  public LinkedAccountServiceImpl(Validator validator, LogEntryService logEntryService) {
    super(validator, logEntryService);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PagedList<LinkedAccount> findBy(LinkedAccountCriteria criteria) {
    return LinkedAccount.findBy(criteria);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LinkedAccount byId(Long id, String... fetches) {
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
