package services.api.impl;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;

import criterias.MessageCriteria;
import models.Message;
import models.Scope;
import play.libs.Json;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.api.MessageApiService;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class MessageApiServiceImpl extends
    AbstractApiService<Message, UUID, MessageCriteria, dto.Message> implements MessageApiService {
  private final LocaleService localeService;
  private final KeyService keyService;

  /**
   * @param messageService
   */
  @Inject
  protected MessageApiServiceImpl(MessageService messageService, LocaleService localeService,
      KeyService keyService) {
    super(messageService, dto.Message.class, dto.Message::from,
        new Scope[] {Scope.ProjectRead, Scope.MessageRead},
        new Scope[] {Scope.ProjectRead, Scope.MessageWrite});
    this.localeService = localeService;
    this.keyService = keyService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Message toModel(JsonNode json) {
    dto.Message dto = Json.fromJson(json, dto.Message.class);

    return dto.toModel(localeService.byId(dto.localeId), keyService.byId(dto.keyId));
  }
}
