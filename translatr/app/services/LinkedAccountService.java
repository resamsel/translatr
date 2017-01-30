package services;

import com.google.inject.ImplementedBy;

import criterias.LinkedAccountCriteria;
import models.LinkedAccount;
import services.impl.LinkedAccountServiceImpl;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(LinkedAccountServiceImpl.class)
public interface LinkedAccountService
    extends ModelService<LinkedAccount, Long, LinkedAccountCriteria> {
  /**
   * @param linkedAccount
   * @return
   */
  LinkedAccount create(LinkedAccount linkedAccount);
}
