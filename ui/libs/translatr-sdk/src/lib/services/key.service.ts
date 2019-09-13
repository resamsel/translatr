import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { convertTemporals } from '../shared/mapper-utils';
import { AbstractService } from './abstract.service';
import { Key, KeyCriteria } from '@dev/translatr-model';
import { ErrorHandler } from './error-handler';

@Injectable({
  providedIn: 'root'
})
export class KeyService extends AbstractService<Key, KeyCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler
  ) {
    super(http, errorHandler, (criteria: KeyCriteria) => `/api/project/${criteria.projectId}/keys`, '/api/key');
  }

  byOwnerAndProjectNameAndName(options: {
    username: string;
    projectName: string;
    keyName: string;
    params?:
      | HttpParams
      | {
          [param: string]: string | string[];
        };
  }): Observable<Key> {
    return this.http
      .get<Key>(
        `/api/${options.username}/${options.projectName}/keys/${
          options.keyName
        }`,
        options
      )
      .pipe(map(convertTemporals));
  }
}
