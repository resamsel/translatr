import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractService } from './abstract.service';
import { AccessToken, RequestCriteria } from '@dev/translatr-model';
import { ErrorHandler } from './error-handler';

export interface AccessTokenCriteria extends RequestCriteria {
  userId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AccessTokenService extends AbstractService<AccessToken,
  AccessTokenCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler
  ) {
    super(http, errorHandler, () => '/api/accesstokens', '/api/accesstoken');
  }
}
