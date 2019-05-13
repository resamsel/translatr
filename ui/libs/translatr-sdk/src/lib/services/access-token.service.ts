import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractService } from './abstract.service';
import { AccessToken, RequestCriteria } from '@dev/translatr-model';
import { Router } from '@angular/router';

export interface AccessTokenCriteria extends RequestCriteria {
  userId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AccessTokenService extends AbstractService<
  AccessToken,
  AccessTokenCriteria
> {
  constructor(http: HttpClient, router: Router) {
    super(http, router, () => '/api/accesstokens', '/api/accesstoken');
  }
}
