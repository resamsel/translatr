import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Locale, LocaleCriteria } from '@dev/translatr-model';
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
export class LocaleService extends AbstractService<Locale, LocaleCriteria> {
  constructor(http: HttpClient, errorHandler: ErrorHandler, languageProvider: LanguageProvider) {
    super(
      http,
      errorHandler,
      languageProvider,
      (criteria: LocaleCriteria) => `/api/project/${criteria.projectId}/locales`,
      '/api/locale'
    );
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
    const path = `/api/${encodePathParam(options.username)}/${encodePathParam(
      options.projectName
    )}/locales/${encodePathParam(options.localeName)}`;
    return this.http
      .get<Locale>(path, {
        headers: {
          [HttpHeader.AcceptLanguage]: this.languageProvider.getActiveLang()
        },
        params: options.params
      })
      .pipe(
        map((locale: Locale) => convertTemporals(locale)),
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
