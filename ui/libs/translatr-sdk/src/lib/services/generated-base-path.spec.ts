import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AccessTokensService } from '../generated/api/accessTokens.service';
import { BASE_PATH } from '../generated/variables';

/**
 * The generated BaseService defaults to an absolute `http://localhost` base
 * path. `TranslatrSdkModule` provides `{ provide: BASE_PATH, useValue: '' }` so
 * injected clients issue same-origin relative URLs. This asserts that provider
 * has the intended effect.
 */
describe('generated API client base path', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [{ provide: BASE_PATH, useValue: '' }],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('issues a relative /api URL, not http://localhost', () => {
    const client = TestBed.inject(AccessTokensService);
    client.findAccessTokens().subscribe();

    httpMock.expectOne('/api/accesstokens').flush({
      list: [],
      total: 0,
      offset: 0,
      limit: 20,
      hasNext: false,
      hasPrev: false,
    });
  });
});
