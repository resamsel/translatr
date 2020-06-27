import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Message, MessageCriteria } from '@dev/translatr-model';
import { AbstractService } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root'
})
export class MessageService extends AbstractService<Message, MessageCriteria> {
  constructor(http: HttpClient, errorHandler: ErrorHandler, languageProvider: LanguageProvider) {
    super(
      http,
      errorHandler,
      languageProvider,
      (criteria: MessageCriteria) => `/api/project/${criteria.projectId}/messages`,
      '/api/message'
    );
  }
}
