import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AuthClientService } from './auth-client.service';

describe('AuthProviderService', () => {
  const http = { get: jest.fn().mockReturnValue(of([])) };

  beforeEach(() => {
    http.get.mockClear();
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => http }]
    });
  });

  it('should be created', () => {
    const service: AuthClientService = TestBed.inject(AuthClientService);
    expect(service).toBeTruthy();
  });

  it('getProviderStatus GETs /api/oidc-providers', () => {
    const service: AuthClientService = TestBed.inject(AuthClientService);

    service.getProviderStatus().subscribe();

    expect(http.get).toHaveBeenCalledWith('/api/oidc-providers');
  });
});
