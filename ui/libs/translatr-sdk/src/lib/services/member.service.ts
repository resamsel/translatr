import { Inject, Injectable, Optional } from '@angular/core';
import { AbstractService } from '@dev/translatr-sdk';
import { Member, MemberCriteria } from '@dev/translatr-model';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { LOGIN_URL } from '@translatr/utils';

@Injectable({
  providedIn: 'root'
})
export class MemberService extends AbstractService<Member, MemberCriteria> {
  constructor(
    http: HttpClient,
    router: Router = undefined,
    @Inject(LOGIN_URL) @Optional() loginUrl: string = undefined
  ) {
    super(
      http,
      router,
      loginUrl,
      (criteria: MemberCriteria) => `/api/members/${criteria.projectId}`,
      '/api/member'
    );
  }
}
