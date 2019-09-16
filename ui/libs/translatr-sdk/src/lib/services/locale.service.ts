import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { convertTemporals } from '../shared/mapper-utils';
import { AbstractService } from './abstract.service';
import { Locale, LocaleCriteria } from '@dev/translatr-model';
import { ErrorHandler } from './error-handler';

@Injectable({
  providedIn: 'root'
})
export class LocaleService extends AbstractService<Locale, LocaleCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler
  ) {
    super(http, errorHandler, (criteria: LocaleCriteria) => `/api/project/${criteria.projectId}/locales`, '/api/locale');
  }

  byOwnerAndProjectNameAndName(options: {
    username: string;
    projectName: string;
    localeName: string;
    params?:
      | HttpParams
      | {
          [param: string]: string | string[];
        };
  }): Observable<Locale> {
    return this.http
      .get<Locale>(
        `/api/${options.username}/${options.projectName}/locales/${
          options.localeName
        }`,
        { params: options.params }
      )
      .pipe(map((locale: Locale) => convertTemporals(locale)));
  }
}
