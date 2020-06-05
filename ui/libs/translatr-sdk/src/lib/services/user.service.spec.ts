import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler } from '@dev/translatr-sdk';

import { UserService } from './user.service';

describe('UserService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler]
    })
  );

  it('should be created', () => {
    const service: UserService = TestBed.get(UserService);
    expect(service).toBeTruthy();
  });
});
