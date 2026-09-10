import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler, LanguageProvider } from '@dev/translatr-sdk';
import { of } from 'rxjs';

import { ProjectService } from './project.service';
import { ProjectsService } from '../generated/api/projects.service';

describe('ProjectService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider],
    }),
  );

  it('should be created', () => {
    const service: ProjectService = TestBed.inject(ProjectService);
    expect(service).toBeTruthy();
  });
});

describe('ProjectService — generated client delegation', () => {
  let service: ProjectService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErrorHandler, LanguageProvider],
    });
    service = TestBed.inject(ProjectService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.match(() => true).forEach((r) => !r.cancelled && r.flush({})));

  it('find() calls ProjectsService.findProjects with shared + resource criteria mapped positionally', () => {
    const spy = jest
      .spyOn(ProjectsService.prototype, 'findProjects')
      .mockReturnValue(
        of({ list: [], total: 0, offset: 0, limit: 20, hasNext: false, hasPrev: false }) as never,
      );

    service
      .find({
        search: 's',
        offset: 5,
        limit: 10,
        order: 'name',
        fetch: 'f',
        ownerId: 'o1',
        owner: 'alice',
        memberId: 'm1',
      })
      .subscribe({ error: () => undefined });

    // findProjects(search, offset, limit, order, fetch, ownerId, ownerUsername, memberId, name)
    expect(spy).toHaveBeenCalled();
    const args = spy.mock.calls[0];
    expect(args.slice(0, 6)).toEqual(['s', 5, 10, 'name', 'f', 'o1']);
    expect(args[6]).toBe('alice'); // criteria.owner -> ownerUsername positional
    expect(args[7]).toBe('m1'); // criteria.memberId -> memberId positional
  });

  it('get() calls ProjectsService.getProject with the id', () => {
    const spy = jest
      .spyOn(ProjectsService.prototype, 'getProject')
      .mockReturnValue(of({ id: 'p1' }) as never);
    service.get('p1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('p1');
  });

  it('create() calls ProjectsService.createProject with the dto', () => {
    const spy = jest
      .spyOn(ProjectsService.prototype, 'createProject')
      .mockReturnValue(of({ id: 'p1' }) as never);
    const dto = { name: 'proj' } as never;
    service.create(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('update() calls ProjectsService.updateProject with the dto', () => {
    const spy = jest
      .spyOn(ProjectsService.prototype, 'updateProject')
      .mockReturnValue(of({ id: 'p1' }) as never);
    const dto = { id: 'p1', name: 'proj2' } as never;
    service.update(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('delete() calls ProjectsService.deleteProject with the id', () => {
    const spy = jest
      .spyOn(ProjectsService.prototype, 'deleteProject')
      .mockReturnValue(of({ id: 'p1' }) as never);
    service.delete('p1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('p1');
  });

  it('byOwnerAndName() still converts nested member temporals to Date', () => {
    let out: { members?: Array<{ whenCreated?: unknown }> } | undefined;
    service.byOwnerAndName('alice', 'proj').subscribe((v) => (out = v as never));

    httpMock.expectOne('/api/alice/proj').flush({
      id: 'p1',
      whenCreated: '2026-01-02T03:04:05.000Z',
      members: [{ id: 1, whenCreated: '2026-01-02T03:04:05.000Z' }],
    });

    expect(out?.members?.[0].whenCreated).toBeInstanceOf(Date);
  });

  it('leaves the bespoke activity() call on HttpClient (regression guard — stays green)', () => {
    service.activity('p1').subscribe();
    const req = httpMock.expectOne('/api/project/p1/activity');
    expect(req.request.method).toBe('GET');
    req.flush({ list: [], total: 0, offset: 0, limit: 20, hasNext: false, hasPrev: false });
  });
});
