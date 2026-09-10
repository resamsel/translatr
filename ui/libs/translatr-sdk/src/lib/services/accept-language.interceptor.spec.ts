import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

// RED: this symbol does not exist yet. `opsx:apply` must add
// `AcceptLanguageInterceptor` in libs/translatr-sdk/src/lib/services/ and
// export it from the sdk barrel.
import { AcceptLanguageInterceptor, LanguageProvider } from '@dev/translatr-sdk';

describe('AcceptLanguageInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let lang: LanguageProvider;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        LanguageProvider,
        { provide: HTTP_INTERCEPTORS, useClass: AcceptLanguageInterceptor, multi: true },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    lang = TestBed.inject(LanguageProvider);
  });

  afterEach(() => httpMock.verify());

  it('sets Accept-Language from the active language on a same-origin API request', () => {
    jest.spyOn(lang, 'getActiveLang').mockReturnValue('de');

    http.get('/api/projects').subscribe();

    const req = httpMock.expectOne('/api/projects');
    expect(req.request.headers.get('Accept-Language')).toBe('de');
    req.flush({});
  });

  it('reflects a later language change on the next request', () => {
    const spy = jest.spyOn(lang, 'getActiveLang').mockReturnValue('en');
    http.get('/api/projects').subscribe();
    httpMock.expectOne('/api/projects').flush({});

    spy.mockReturnValue('fr');
    http.get('/api/keys').subscribe();
    const req = httpMock.expectOne('/api/keys');
    expect(req.request.headers.get('Accept-Language')).toBe('fr');
    req.flush({});
  });

  it('sets exactly one Accept-Language header value', () => {
    jest.spyOn(lang, 'getActiveLang').mockReturnValue('en');
    http.get('/api/projects').subscribe();
    const req = httpMock.expectOne('/api/projects');
    expect(req.request.headers.getAll('Accept-Language')).toEqual(['en']);
    req.flush({});
  });

  it('does not attach Accept-Language to a third-party absolute URL', () => {
    jest.spyOn(lang, 'getActiveLang').mockReturnValue('en');
    http.get('https://accounts.google.com/o/oauth2/v2/auth').subscribe();
    const req = httpMock.expectOne('https://accounts.google.com/o/oauth2/v2/auth');
    expect(req.request.headers.has('Accept-Language')).toBe(false);
    req.flush({});
  });
});
