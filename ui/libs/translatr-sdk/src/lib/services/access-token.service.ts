import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AccessToken, AccessTokenCriteria } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { AccessTokensService } from '../generated/api/accessTokens.service';
import { AccessTokenDto } from '../generated/model/accessTokenDto';
import { AbstractService, PagedListLike } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root',
})
export class AccessTokenService extends AbstractService<AccessToken, AccessTokenCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler,
    languageProvider: LanguageProvider,
    client: AccessTokensService,
  ) {
    super(http, errorHandler, languageProvider, {
      entityPath: '/api/accesstoken',
      listPath: () => '/api/accesstokens',
      list: (c: AccessTokenCriteria | undefined, context?: HttpContext) =>
        client.findAccessTokens(
          c?.search,
          c?.offset,
          c?.limit,
          c?.order,
          c?.fetch,
          c?.userId,
          'body',
          false,
          { context },
        ) as unknown as Observable<PagedListLike<AccessToken>>,
      get: (id: string | number, context?: HttpContext) =>
        client.getAccessToken(Number(id), 'body', false, {
          context,
        }) as unknown as Observable<AccessToken>,
      create: (dto: AccessToken, context?: HttpContext) =>
        client.createAccessToken(dto as unknown as AccessTokenDto, 'body', false, {
          context,
        }) as unknown as Observable<AccessToken>,
      update: (dto: Partial<AccessToken>, context?: HttpContext) =>
        client.updateAccessToken(dto as unknown as AccessTokenDto, 'body', false, {
          context,
        }) as unknown as Observable<AccessToken>,
      delete: (id: string | number, context?: HttpContext) =>
        client.deleteAccessToken(Number(id), 'body', false, {
          context,
        }) as unknown as Observable<AccessToken>,
    });
  }
}
