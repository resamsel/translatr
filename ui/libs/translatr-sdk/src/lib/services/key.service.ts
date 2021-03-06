import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Key, KeyCriteria } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { convertTemporals } from '../shared/mapper-utils';
import { AbstractService, encodePathParam } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { HttpHeader } from './http-header';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root'
})
export class KeyService extends AbstractService<Key, KeyCriteria> {
  constructor(http: HttpClient, errorHandler: ErrorHandler, languageProvider: LanguageProvider) {
    super(
      http,
      errorHandler,
      languageProvider,
      (criteria: KeyCriteria) => `/api/project/${criteria.projectId}/keys`,
      '/api/key'
    );
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
    const path = `/api/${options.username}/${options.projectName}/keys/${encodePathParam(
      options.keyName
    )}`;
    return this.http
      .get<Key>(path, {
        headers: {
          [HttpHeader.AcceptLanguage]: this.languageProvider.getActiveLang()
        },
        ...options
      })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'byOwnerAndProjectNameAndName',
            params: [options],
            method: 'get',
            path
          })
        )
      );
  }
}
