import { Inject, Injectable, Optional } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractService } from './abstract.service';
import { Message } from '@dev/translatr-model';
import { Router } from '@angular/router';
import { LOGIN_URL } from '@translatr/utils';
import { MessageCriteria } from '@translatr/translatr-model/src/lib/model/message-criteria';

@Injectable({
  providedIn: 'root'
})
export class MessageService extends AbstractService<Message, MessageCriteria> {
  constructor(
    http: HttpClient,
    router?: Router,
    @Inject(LOGIN_URL) @Optional() loginUrl?: string
  ) {
    super(
      http,
      router,
      loginUrl,
      (criteria: MessageCriteria) => `/api/project/${criteria.projectId}/messages`,
      '/api/message'
    );
  }
}
