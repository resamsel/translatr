import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractService } from './abstract.service';
import { Message, RequestCriteria } from '@dev/translatr-model';

@Injectable({
  providedIn: 'root'
})
export class MessageService extends AbstractService<Message, RequestCriteria> {

  constructor(http: HttpClient) {
    super(http, '/api/messages', '/api/message');
  }
}
