import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractService } from './abstract.service';
import { Message, RequestCriteria } from '@dev/translatr-model';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class MessageService extends AbstractService<Message, RequestCriteria> {
  constructor(http: HttpClient, router: Router) {
    super(http, router, () => '/api/messages', '/api/message');
  }
}
