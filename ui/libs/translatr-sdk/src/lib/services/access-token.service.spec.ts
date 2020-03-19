import { TestBed } from '@angular/core/testing';
import { AccessTokenService, ErrorHandler } from '@dev/translatr-sdk';
import { HttpClient } from '@angular/common/http';

describe('AccessTokenServiceService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      { provide: HttpClient, useFactory: () => ({}) },
      ErrorHandler
    ]
  }));

  it('should be created', () => {
    const service: AccessTokenService = TestBed.get(AccessTokenService);
    expect(service).toBeTruthy();
  });
});
