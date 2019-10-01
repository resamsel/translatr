import { TestBed } from '@angular/core/testing';

import { AuthProviderService } from './auth-provider.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';

describe('AuthProviderService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientTestingModule
    ],
    providers: [
      {
        provide: AuthProviderService,
        useFactory: (http: HttpClient) => new AuthProviderService(http),
        deps: [HttpClient]
      }
    ]
  }));

  it('should be created', () => {
    const service: AuthProviderService = TestBed.get(AuthProviderService);
    expect(service).toBeTruthy();
  });
});
