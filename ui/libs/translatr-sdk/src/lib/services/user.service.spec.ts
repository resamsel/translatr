import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler, LanguageProvider } from '@dev/translatr-sdk';
import { of } from 'rxjs';

import { UserService } from './user.service';
import { UsersService } from '../generated/api/users.service';

describe('UserService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider],
    }),
  );

  it('should be created', () => {
    const service: UserService = TestBed.inject(UserService);
    expect(service).toBeTruthy();
  });
});

describe('UserService — generated client delegation', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErrorHandler, LanguageProvider],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.match(() => true).forEach((r) => !r.cancelled && r.flush({})));

  it('find() calls UsersService.findUsers with the shared criteria mapped positionally', () => {
    const spy = jest
      .spyOn(UsersService.prototype, 'findUsers')
      .mockReturnValue(
        of({ list: [], total: 0, offset: 0, limit: 20, hasNext: false, hasPrev: false }) as never,
      );

    service
      .find({ search: 's', offset: 5, limit: 10, order: 'name', fetch: 'f' })
      .subscribe({ error: () => undefined });

    // findUsers(search, offset, limit, order, fetch, username, email)
    expect(spy).toHaveBeenCalled();
    expect(spy.mock.calls[0].slice(0, 5)).toEqual(['s', 5, 10, 'name', 'f']);
  });

  it('get() calls UsersService.getUser with the id', () => {
    const spy = jest
      .spyOn(UsersService.prototype, 'getUser')
      .mockReturnValue(of({ id: 'u1' }) as never);
    service.get('u1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('u1');
  });

  it('update() calls UsersService.updateUser with the dto', () => {
    const spy = jest
      .spyOn(UsersService.prototype, 'updateUser')
      .mockReturnValue(of({ id: 'u1' }) as never);
    const dto = { id: 'u1', name: 'New Name' } as never;
    service.update(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('delete() calls UsersService.deleteUser with the id', () => {
    const spy = jest
      .spyOn(UsersService.prototype, 'deleteUser')
      .mockReturnValue(of({ id: 'u1' }) as never);
    service.delete('u1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('u1');
  });

  it('create() calls UsersService.createUser with the dto', () => {
    const spy = jest
      .spyOn(UsersService.prototype, 'createUser')
      .mockReturnValue(of({ id: 'u1' }) as never);
    const dto = { username: 'newuser', name: 'New User' } as never;
    service.create(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('leaves bespoke me() on HttpClient (regression guard — stays green)', () => {
    service.me().subscribe();
    const req = httpMock.expectOne('/api/me');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
