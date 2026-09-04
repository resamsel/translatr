import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthClientService } from './auth-client.service';

describe('AuthClientService', () => {
  let service: AuthClientService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(AuthClientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('find() GETs /api/authclients', () => {
    service.find().subscribe();

    const req = httpMock.expectOne('/api/authclients');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getProviderStatus() GETs the relative path /api/oidc-providers, not an absolute http://localhost URL', () => {
    service.getProviderStatus().subscribe();

    // Regression test: the generated OidcProvidersService's BaseService
    // falls back to a hardcoded absolute basePath ('http://localhost')
    // whenever no BASE_PATH/Configuration is supplied. AuthClientService
    // must construct it with an explicit relative basePath so requests
    // still go to whatever origin the app is actually served from.
    const req = httpMock.expectOne('/api/oidc-providers');
    expect(req.request.url).toBe('/api/oidc-providers');
    expect(req.request.url).not.toContain('localhost');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
