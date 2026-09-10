import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Message, MessageCriteria } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { MessagesService } from '../generated/api/messages.service';
import { MessageDto } from '../generated/model/messageDto';
import { AbstractService, PagedListLike } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root',
})
export class MessageService extends AbstractService<Message, MessageCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler,
    languageProvider: LanguageProvider,
    client: MessagesService,
  ) {
    super(http, errorHandler, languageProvider, {
      entityPath: '/api/message',
      listPath: (criteria?: MessageCriteria) => `/api/project/${criteria?.projectId}/messages`,
      list: (c: MessageCriteria | undefined, context?: HttpContext) =>
        client.findMessagesByProject(
          String(c?.projectId),
          c?.search,
          c?.offset,
          c?.limit,
          c?.order,
          c?.fetch,
          c?.localeId,
          c?.localeIds,
          undefined,
          c?.keyIds,
          c?.keyName,
          'body',
          false,
          { context },
        ) as unknown as Observable<PagedListLike<Message>>,
      get: (id: string | number, context?: HttpContext) =>
        client.getMessage(String(id), 'body', false, { context }) as unknown as Observable<Message>,
      create: (dto: Message, context?: HttpContext) =>
        client.createMessage(dto as unknown as MessageDto, 'body', false, {
          context,
        }) as unknown as Observable<Message>,
      update: (dto: Partial<Message>, context?: HttpContext) =>
        client.updateMessage(dto as unknown as MessageDto, 'body', false, {
          context,
        }) as unknown as Observable<Message>,
      delete: (id: string | number, context?: HttpContext) =>
        client.deleteMessage(String(id), 'body', false, {
          context,
        }) as unknown as Observable<Message>,
    });
  }
}
