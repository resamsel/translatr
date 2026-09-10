import { HttpClient, HttpContext, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Locale, LocaleCriteria } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { convertTemporals } from '../shared/mapper-utils';
import { LocalesService } from '../generated/api/locales.service';
import { LocaleDto } from '../generated/model/localeDto';
import { AbstractService, PagedListLike, encodePathParam } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root',
})
export class LocaleService extends AbstractService<Locale, LocaleCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler,
    languageProvider: LanguageProvider,
    client: LocalesService,
  ) {
    super(http, errorHandler, languageProvider, {
      entityPath: '/api/locale',
      listPath: (criteria?: LocaleCriteria) => `/api/project/${criteria?.projectId}/locales`,
      list: (c: LocaleCriteria | undefined, context?: HttpContext) =>
        client.findLocalesByProject(
          String(c?.projectId),
          c?.search,
          c?.offset,
          c?.limit,
          c?.order,
          c?.fetch,
          c?.keyId,
          c?.missing,
          undefined,
          'body',
          false,
          { context },
        ) as unknown as Observable<PagedListLike<Locale>>,
      get: (id: string | number, context?: HttpContext) =>
        client.getLocale(String(id), 'body', false, { context }) as unknown as Observable<Locale>,
      create: (dto: Locale, context?: HttpContext) =>
        client.createLocale(dto as unknown as LocaleDto, 'body', false, {
          context,
        }) as unknown as Observable<Locale>,
      update: (dto: Partial<Locale>, context?: HttpContext) =>
        client.updateLocale(dto as unknown as LocaleDto, 'body', false, {
          context,
        }) as unknown as Observable<Locale>,
      delete: (id: string | number, context?: HttpContext) =>
        client.deleteLocale(String(id), 'body', false, {
          context,
        }) as unknown as Observable<Locale>,
    });
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
      options.projectName,
    )}/locales/${encodePathParam(options.localeName)}`;
    return this.http
      .get<Locale>(path, {
        context: this.authContext,
        params: options.params,
      })
      .pipe(
        map((locale: Locale) => convertTemporals(locale)),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'byOwnerAndProjectNameAndName',
            params: [options],
            method: 'get',
            path,
          }),
        ),
      );
  }
}
