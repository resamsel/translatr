import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { AccessTokenService, ErrorHandler, LanguageProvider } from '@dev/translatr-sdk';

describe('AccessTokenServiceService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider]
    })
  );

  it('should be created', () => {
    const service: AccessTokenService = TestBed.get(AccessTokenService);
    expect(service).toBeTruthy();
  });
});
