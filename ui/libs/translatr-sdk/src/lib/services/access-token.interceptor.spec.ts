import { HTTP_INTERCEPTORS, HttpClient, HttpContext } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ACCESS_TOKEN, AccessTokenInterceptor } from '@dev/translatr-sdk';

describe('AccessTokenInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [{ provide: HTTP_INTERCEPTORS, useClass: AccessTokenInterceptor, multi: true }],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('adds ?access_token= when the context carries a token', () => {
    const context = new HttpContext().set(ACCESS_TOKEN, 'tok-123');
    http.get('/api/keys', { context }).subscribe();

    const req = httpMock.expectOne((r) => r.url === '/api/keys');
    expect(req.request.params.get('access_token')).toBe('tok-123');
    req.flush({});
  });

  it('leaves the request untouched when no token is in the context', () => {
    http.get('/api/keys').subscribe();

    const req = httpMock.expectOne('/api/keys');
    expect(req.request.params.has('access_token')).toBe(false);
    req.flush({});
  });

  it('does not add the token to a third-party absolute URL', () => {
    const context = new HttpContext().set(ACCESS_TOKEN, 'tok-123');
    http.get('https://accounts.google.com/o/oauth2/v2/auth', { context }).subscribe();

    const req = httpMock.expectOne('https://accounts.google.com/o/oauth2/v2/auth');
    expect(req.request.params.has('access_token')).toBe(false);
    req.flush({});
  });
});
