import { Inject, Injectable, Optional } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractService } from './abstract.service';
import { Message, RequestCriteria } from '@dev/translatr-model';
import { Router } from '@angular/router';
import { LOGIN_URL } from '@translatr/utils';

@Injectable({
  providedIn: 'root'
})
export class MessageService extends AbstractService<Message, RequestCriteria> {
  constructor(
    http: HttpClient,
    router: Router = undefined,
    @Inject(LOGIN_URL) @Optional() loginUrl: string = undefined
  ) {
    super(http, router, loginUrl, () => '/api/messages', '/api/message');
  }
}
