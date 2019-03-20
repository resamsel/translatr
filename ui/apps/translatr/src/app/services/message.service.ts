import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { Key } from "../shared/key";
import { map } from "rxjs/operators";
import { convertTemporals, convertTemporalsList } from "../shared/mapper-utils";
import { PagedList } from "../shared/paged-list";
import { Message } from "../shared/message";

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  constructor(private readonly http: HttpClient) {
  }

  getMessages(options: {
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
            ...options.options && options.options.params ? options.options.params: {},
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

  create(message: Message): Observable<Message> {
    return this.http
      .post<Message>('/api/message', message)
      .pipe(map(convertTemporals));
  }

  update(message: Message): Observable<Message> {
    return this.http
      .put<Message>('/api/message', message)
      .pipe(map(convertTemporals));
  }
}
