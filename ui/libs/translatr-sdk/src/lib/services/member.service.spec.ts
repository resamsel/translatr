import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler, LanguageProvider } from '@dev/translatr-sdk';
import { of } from 'rxjs';

import { MemberService } from './member.service';
import { MembersService } from '../generated/api/members.service';

describe('MemberService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider],
    }),
  );

  it('should be created', () => {
    const service: MemberService = TestBed.inject(MemberService);
    expect(service).toBeTruthy();
  });
});

describe('MemberService — generated client delegation', () => {
  let service: MemberService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErrorHandler, LanguageProvider],
    });
    service = TestBed.inject(MemberService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.match(() => true).forEach((r) => !r.cancelled && r.flush({})));

  it('find() calls MembersService.findMembersByProject with projectId + shared criteria positionally', () => {
    const spy = jest
      .spyOn(MembersService.prototype, 'findMembersByProject')
      .mockReturnValue(
        of({ list: [], total: 0, offset: 0, limit: 20, hasNext: false, hasPrev: false }) as never,
      );

    service
      .find({ projectId: 'pr1', search: 's', offset: 5, limit: 10, order: 'name', fetch: 'f' })
      .subscribe({ error: () => undefined });

    // findMembersByProject(projectId, search, offset, limit, order, fetch, userId)
    expect(spy).toHaveBeenCalled();
    expect(spy.mock.calls[0].slice(0, 6)).toEqual(['pr1', 's', 5, 10, 'name', 'f']);
  });

  // MemberCriteria.roles has no positional slot on the generated
  // findMembersByProject signature. apply/design must decide: extend the
  // contract with a `roles` query param, or keep that filter on HttpClient.
  it.skip('find() forwards MemberCriteria.roles (no generated param exists yet)', () => {
    /* contract gap — see report */
  });

  it('get() calls MembersService.getMember with the numeric id', () => {
    const spy = jest
      .spyOn(MembersService.prototype, 'getMember')
      .mockReturnValue(of({ id: 5 }) as never);
    service.get(5).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe(5);
  });

  it('create() calls MembersService.createMember with the dto', () => {
    const spy = jest
      .spyOn(MembersService.prototype, 'createMember')
      .mockReturnValue(of({ id: 'm1' }) as never);
    const dto = { projectId: 'pr1', userId: 'u1', role: 'Owner' } as never;
    service.create(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('update() calls MembersService.updateMember with the dto', () => {
    const spy = jest
      .spyOn(MembersService.prototype, 'updateMember')
      .mockReturnValue(of({ id: 'm1' }) as never);
    const dto = { id: 'm1', role: 'Translator' } as never;
    service.update(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('delete() calls MembersService.deleteMember with the numeric id', () => {
    const spy = jest
      .spyOn(MembersService.prototype, 'deleteMember')
      .mockReturnValue(of({ id: 5 }) as never);
    service.delete(5).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe(5);
  });
});
