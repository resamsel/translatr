import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler, FeatureFlagService, LanguageProvider } from '@dev/translatr-sdk';
import { of } from 'rxjs';
import { UserFeatureFlagsService } from '../generated/api/userFeatureFlags.service';

describe('FeatureFlagService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider],
    }),
  );

  it('should be created', () => {
    const service: FeatureFlagService = TestBed.inject(FeatureFlagService);
    expect(service).toBeTruthy();
  });
});

describe('FeatureFlagService (HTTP)', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErrorHandler, LanguageProvider],
    }),
  );

  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('resolved() GETs /api/featureflags/resolved', () => {
    const service = TestBed.inject(FeatureFlagService);
    const http = TestBed.inject(HttpTestingController);
    service.resolved().subscribe();
    const req = http.expectOne('/api/featureflags/resolved');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});

describe('FeatureFlagService — generated client delegation', () => {
  let service: FeatureFlagService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ErrorHandler, LanguageProvider],
    });
    service = TestBed.inject(FeatureFlagService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.match(() => true).forEach((r) => !r.cancelled && r.flush({})));

  it('find() calls UserFeatureFlagsService.findUserFeatureFlags with the criteria mapped positionally', () => {
    const spy = jest
      .spyOn(UserFeatureFlagsService.prototype, 'findUserFeatureFlags')
      .mockReturnValue(
        of({ list: [], total: 0, offset: 0, limit: 20, hasNext: false, hasPrev: false }) as never,
      );

    service
      .find({ search: 's', offset: 5, limit: 10, order: 'name', fetch: 'f', userId: 'u1' })
      .subscribe({ error: () => undefined });

    // findUserFeatureFlags(search, offset, limit, order, fetch, userId, feature)
    expect(spy).toHaveBeenCalled();
    expect(spy.mock.calls[0].slice(0, 6)).toEqual(['s', 5, 10, 'name', 'f', 'u1']);
  });

  it('get() calls UserFeatureFlagsService.getUserFeatureFlag with the id', () => {
    const spy = jest
      .spyOn(UserFeatureFlagsService.prototype, 'getUserFeatureFlag')
      .mockReturnValue(of({ id: 'ff1' }) as never);
    service.get('ff1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('ff1');
  });

  it('create() calls UserFeatureFlagsService.createUserFeatureFlag with the dto', () => {
    const spy = jest
      .spyOn(UserFeatureFlagsService.prototype, 'createUserFeatureFlag')
      .mockReturnValue(of({ id: 'ff1' }) as never);
    const dto = { userId: 'u1', feature: 'editor', enabled: true } as never;
    service.create(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('update() calls UserFeatureFlagsService.updateUserFeatureFlag with the dto', () => {
    const spy = jest
      .spyOn(UserFeatureFlagsService.prototype, 'updateUserFeatureFlag')
      .mockReturnValue(of({ id: 'ff1' }) as never);
    const dto = { id: 'ff1', enabled: false } as never;
    service.update(dto).subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toEqual(dto);
  });

  it('delete() calls UserFeatureFlagsService.deleteUserFeatureFlag with the id', () => {
    const spy = jest
      .spyOn(UserFeatureFlagsService.prototype, 'deleteUserFeatureFlag')
      .mockReturnValue(of({ id: 'ff1' }) as never);
    service.delete('ff1').subscribe({ error: () => undefined });
    expect(spy.mock.calls[0][0]).toBe('ff1');
  });
});
