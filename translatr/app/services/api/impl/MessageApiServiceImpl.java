package services.api.impl;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import criterias.MessageCriteria;
import models.Message;
import models.Scope;
import services.MessageService;
import services.api.MessageApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class MessageApiServiceImpl extends
    AbstractApiService<Message, UUID, MessageCriteria, dto.Message> implements MessageApiService {
  /**
   * @param localeService
   */
  @Inject
  protected MessageApiServiceImpl(MessageService localeService) {
    super(localeService, dto.Message.class, dto.Message::from, Message::from,
        new Scope[] {Scope.ProjectRead, Scope.MessageRead},
        new Scope[] {Scope.ProjectRead, Scope.MessageWrite});
  }
}
