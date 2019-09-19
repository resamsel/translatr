import { Injectable } from '@angular/core';
import { AbstractService } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { Member, MemberCriteria } from '@dev/translatr-model';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class MemberService extends AbstractService<Member, MemberCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler
  ) {
    super(
      http,
      errorHandler,
      (criteria: MemberCriteria) => `/api/project/${criteria.projectId}/members`,
      '/api/member'
    );
  }
}
