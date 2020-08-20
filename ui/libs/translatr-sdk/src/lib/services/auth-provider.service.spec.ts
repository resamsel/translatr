import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { AuthClientService } from './auth-client.service';

describe('AuthProviderService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }]
    })
  );

  it('should be created', () => {
    const service: AuthClientService = TestBed.get(AuthClientService);
    expect(service).toBeTruthy();
  });
});
