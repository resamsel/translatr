import { Inject, Injectable, Optional } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractService } from './abstract.service';
import { AccessToken, RequestCriteria } from '@dev/translatr-model';
import { Router } from '@angular/router';
import { LOGIN_URL } from '@translatr/utils';

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
    router?: Router,
    @Inject(LOGIN_URL) @Optional() loginUrl?: string
  ) {
    super(http, router, loginUrl, () => '/api/accesstokens', '/api/accesstoken');
  }
}
