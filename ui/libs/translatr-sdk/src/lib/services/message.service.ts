import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractService } from './abstract.service';
import { Message } from '@dev/translatr-model';
import { MessageCriteria } from '@translatr/translatr-model/src/lib/model/message-criteria';
import { ErrorHandler } from './error-handler';

@Injectable({
  providedIn: 'root'
})
export class MessageService extends AbstractService<Message, MessageCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler
  ) {
    super(http, errorHandler, (criteria: MessageCriteria) => `/api/project/${criteria.projectId}/messages`, '/api/message');
  }
}
