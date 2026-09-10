import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AccessTokenService, ErrorHandler, LanguageProvider } from '@dev/translatr-sdk';
import { of } from 'rxjs';
import { AccessTokensService } from '../generated/api/accessTokens.service';

describe('AccessTokenServiceService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider],
    }),
  );

  it('should be created', () => {
    const service: AccessTokenService = TestBed.inject(AccessTokenService);
    expect(service).toBeTruthy();
  });
});

describe('AccessTokenService — generated client delegation', () => {
  let service: AccessTokenService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErrorHandler, LanguageProvider],
    });
    service = TestBed.inject(AccessTokenService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.match(() => true).forEach((r) => !r.cancelled && r.flush({})));

  it('find() calls AccessTokensService.findAccessTokens with the criteria mapped positionally', () => {
    const spy = jest
      .spyOn(AccessTokensService.prototype, 'findAccessTokens')
      .mockReturnValue(
        of({ list: [], total: 0, offset: 0, limit: 20, hasNext: false, hasPrev: false }) as never,
      );

    service
      .find({ search: 's', offset: 5, limit: 10, order: 'name', fetch: 'f', userId: 'u1' })
      .subscribe({ error: () => undefined });

    // findAccessTokens(search, offset, limit, order, fetch, userId)
    expect(spy.mock.calls[0].slice(0, 6)).toEqual(['s', 5, 10, 'name', 'f', 'u1']);
  });

  it('get() calls AccessTokensService.getAccessToken with the id', () => {
    const spy = jest
      .spyOn(AccessTokensService.prototype, 'getAccessToken')
      .mockReturnValue(of({ id: 7 }) as never);

    service.get(7).subscribe({ error: () => undefined });

    expect(spy).toHaveBeenCalled();
    expect(Number(spy.mock.calls[0][0])).toBe(7);
  });

  it('create() calls AccessTokensService.createAccessToken with the dto', () => {
    const spy = jest
      .spyOn(AccessTokensService.prototype, 'createAccessToken')
      .mockReturnValue(of({ id: 1 }) as never);
    const dto = { name: 'n', scope: 'read' } as never;

    service.create(dto).subscribe({ error: () => undefined });

    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('update() calls AccessTokensService.updateAccessToken with the dto', () => {
    const spy = jest
      .spyOn(AccessTokensService.prototype, 'updateAccessToken')
      .mockReturnValue(of({ id: 1 }) as never);
    const dto = { id: 1, name: 'n2' } as never;

    service.update(dto).subscribe({ error: () => undefined });

    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('delete() calls AccessTokensService.deleteAccessToken with the id', () => {
    const spy = jest
      .spyOn(AccessTokensService.prototype, 'deleteAccessToken')
      .mockReturnValue(of({ id: 3 }) as never);

    service.delete(3).subscribe({ error: () => undefined });

    expect(spy).toHaveBeenCalled();
    expect(Number(spy.mock.calls[0][0])).toBe(3);
  });

  it('get() result exposes temporal fields as Date instances', () => {
    jest
      .spyOn(AccessTokensService.prototype, 'getAccessToken')
      .mockReturnValue(
        of({
          id: 1,
          whenCreated: '2026-01-02T03:04:05.000Z',
          whenUpdated: '2026-01-02T03:04:05.000Z',
        }) as never,
      );

    let out: { whenCreated?: unknown } | undefined;
    service.get(1).subscribe((v) => (out = v as never));

    expect(out?.whenCreated).toBeInstanceOf(Date);
  });
});
