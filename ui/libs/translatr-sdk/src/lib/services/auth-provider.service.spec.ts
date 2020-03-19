import { TestBed } from '@angular/core/testing';

import { AuthProviderService } from './auth-provider.service';
import { HttpClient } from '@angular/common/http';

describe('AuthProviderService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      { provide: HttpClient, useFactory: () => ({}) }
    ]
  }));

  it('should be created', () => {
    const service: AuthProviderService = TestBed.get(AuthProviderService);
    expect(service).toBeTruthy();
  });
});
