import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Member, MemberCriteria } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { MembersService } from '../generated/api/members.service';
import { MemberDto } from '../generated/model/memberDto';
import { AbstractService, PagedListLike } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

@Injectable({
  providedIn: 'root',
})
export class MemberService extends AbstractService<Member, MemberCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler,
    languageProvider: LanguageProvider,
    client: MembersService,
  ) {
    super(http, errorHandler, languageProvider, {
      entityPath: '/api/member',
      listPath: (criteria?: MemberCriteria) => `/api/project/${criteria?.projectId}/members`,
      // MemberCriteria.roles has no positional slot on findMembersByProject; the
      // generated `userId` param has no criteria field. See notes.md.
      list: (c: MemberCriteria | undefined, context?: HttpContext) =>
        client.findMembersByProject(
          String(c?.projectId),
          c?.search,
          c?.offset,
          c?.limit,
          c?.order,
          c?.fetch,
          undefined,
          'body',
          false,
          { context },
        ) as unknown as Observable<PagedListLike<Member>>,
      get: (id: string | number, context?: HttpContext) =>
        client.getMember(Number(id), 'body', false, { context }) as unknown as Observable<Member>,
      create: (dto: Member, context?: HttpContext) =>
        client.createMember(dto as unknown as MemberDto, 'body', false, {
          context,
        }) as unknown as Observable<Member>,
      update: (dto: Partial<Member>, context?: HttpContext) =>
        client.updateMember(dto as unknown as MemberDto, 'body', false, {
          context,
        }) as unknown as Observable<Member>,
      delete: (id: string | number, context?: HttpContext) =>
        client.deleteMember(Number(id), 'body', false, {
          context,
        }) as unknown as Observable<Member>,
    });
  }
}
