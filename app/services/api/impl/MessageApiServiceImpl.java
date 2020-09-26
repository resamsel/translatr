package services.api.impl;

import criterias.MessageCriteria;
import mappers.MessageMapper;
import models.Message;
import models.Scope;
import services.KeyService;
import services.LocaleService;
import services.MessageService;
import services.PermissionService;
import services.api.MessageApiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class MessageApiServiceImpl extends
    AbstractApiService<Message, UUID, MessageCriteria, MessageService, dto.Message> implements
    MessageApiService {

  private final LocaleService localeService;
  private final KeyService keyService;

  @Inject
  protected MessageApiServiceImpl(MessageService messageService, LocaleService localeService,
                                  KeyService keyService, PermissionService permissionService, Validator validator,
                                  MessageMapper messageMapper) {
    super(messageService, dto.Message.class, messageMapper::toDto,
        new Scope[]{Scope.ProjectRead, Scope.MessageRead},
        new Scope[]{Scope.ProjectRead, Scope.MessageWrite},
        permissionService,
        validator);

    this.localeService = localeService;
    this.keyService = keyService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Message toModel(dto.Message in) {
    return MessageMapper.toModel(in, localeService.byId(in.localeId, null /* FIXME */), keyService.byId(in.keyId, null /* FIXME */));
  }
}
