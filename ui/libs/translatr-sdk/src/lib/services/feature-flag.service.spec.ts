import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler, FeatureFlagService, LanguageProvider } from '@dev/translatr-sdk';

describe('FeatureFlagService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider]
    })
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
      providers: [ErrorHandler, LanguageProvider]
    })
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
