import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {convertTemporalsList} from '../shared/mapper-utils';
import {AbstractService} from './abstract.service';
import {Message, PagedList, RequestCriteria} from '@dev/translatr-model';

@Injectable({
  providedIn: 'root'
})
export class MessageService extends AbstractService<Message, RequestCriteria> {

  constructor(http: HttpClient) {
    super(http, '/api/messages', '/api/message');
  }

  _getMessages(options: {
    projectId: string;
    localeId?: string;
    keyName?: string;
    options?: {
      params?: HttpParams | RequestCriteria
    }
  }): Observable<PagedList<Message>> {
    return this.http
      .get<PagedList<Message>>(
        `/api/messages/${options.projectId}`,
        {
          params: {
            ...options.options && options.options.params ? options.options.params : {},
            localeId: options.localeId
          }
        })
      .pipe(
        map((payload: PagedList<Message>) => ({
          ...payload,
          list: convertTemporalsList(payload.list)
        }))
      );
  }
}
