import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Member, MemberCriteria } from '@dev/translatr-model';
import { AbstractService } from './abstract.service';
import { ErrorHandler } from './error-handler';

@Injectable({
  providedIn: 'root'
})
export class MemberService extends AbstractService<Member, MemberCriteria> {
  constructor(http: HttpClient, errorHandler: ErrorHandler) {
    super(
      http,
      errorHandler,
      (criteria: MemberCriteria) => `/api/project/${criteria.projectId}/members`,
      '/api/member'
    );
  }
}
