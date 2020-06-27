import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Member, MemberCriteria } from '@dev/translatr-model';
import { AbstractService } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root'
})
export class MemberService extends AbstractService<Member, MemberCriteria> {
  constructor(http: HttpClient, errorHandler: ErrorHandler, languageProvider: LanguageProvider) {
    super(
      http,
      errorHandler,
      languageProvider,
      (criteria: MemberCriteria) => `/api/project/${criteria.projectId}/members`,
      '/api/member'
    );
  }
}
