import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AccessToken, AccessTokenCriteria } from '@dev/translatr-model';
import { AbstractService } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root'
})
export class AccessTokenService extends AbstractService<AccessToken, AccessTokenCriteria> {
  constructor(http: HttpClient, errorHandler: ErrorHandler, languageProvider: LanguageProvider) {
    super(http, errorHandler, languageProvider, () => '/api/accesstokens', '/api/accesstoken');
  }
}
