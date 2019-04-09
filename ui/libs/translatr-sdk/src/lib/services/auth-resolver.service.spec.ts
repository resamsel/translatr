import { TestBed } from '@angular/core/testing';

import { AuthResolverService } from './auth-resolver.service';

describe('AuthResolverService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AuthResolverService = TestBed.get(AuthResolverService);
    expect(service).toBeTruthy();
  });
});
