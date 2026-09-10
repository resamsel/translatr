import { HttpClient, HttpContext, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Key, KeyCriteria } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { convertTemporals } from '../shared/mapper-utils';
import { KeysService } from '../generated/api/keys.service';
import { KeyDto } from '../generated/model/keyDto';
import { AbstractService, PagedListLike, encodePathParam } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root',
})
export class KeyService extends AbstractService<Key, KeyCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler,
    languageProvider: LanguageProvider,
    client: KeysService,
  ) {
    super(http, errorHandler, languageProvider, {
      entityPath: '/api/key',
      listPath: (criteria?: KeyCriteria) => `/api/project/${criteria?.projectId}/keys`,
      list: (c: KeyCriteria | undefined, context?: HttpContext) =>
        client.findKeysByProject(
          String(c?.projectId),
          c?.search,
          c?.offset,
          c?.limit,
          c?.order,
          c?.fetch,
          c?.localeId,
          c?.missing,
          'body',
          false,
          { context },
        ) as unknown as Observable<PagedListLike<Key>>,
      get: (id: string | number, context?: HttpContext) =>
        client.getKey(String(id), 'body', false, { context }) as unknown as Observable<Key>,
      create: (dto: Key, context?: HttpContext) =>
        client.createKey(dto as unknown as KeyDto, 'body', false, {
          context,
        }) as unknown as Observable<Key>,
      update: (dto: Partial<Key>, context?: HttpContext) =>
        client.updateKey(dto as unknown as KeyDto, 'body', false, {
          context,
        }) as unknown as Observable<Key>,
      delete: (id: string | number, context?: HttpContext) =>
        client.deleteKey(String(id), 'body', false, { context }) as unknown as Observable<Key>,
    });
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
      options.keyName,
    )}`;
    return this.http
      .get<Key>(path, {
        context: this.authContext,
        ...options,
      })
      .pipe(
        map(convertTemporals),
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
