package services.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;

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
public class LinkedAccountServiceImpl extends AbstractModelService<LinkedAccount, dto.LinkedAccount>
    implements LinkedAccountService {
  /**
   * @param configuration
   */
  @Inject
  public LinkedAccountServiceImpl(Configuration configuration, LogEntryService logEntryService) {
    super(dto.LinkedAccount.class, configuration, logEntryService);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected LinkedAccount byId(JsonNode id) {
    return LinkedAccount.byId(id.asLong());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected LinkedAccount toModel(dto.LinkedAccount dto) {
    return dto.toModel();
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
