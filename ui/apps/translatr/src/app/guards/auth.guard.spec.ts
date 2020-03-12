import { inject, TestBed } from '@angular/core/testing';

import { AuthGuard } from './auth.guard';
import { AppFacade } from '../+state/app.facade';
import { LOGIN_URL, WINDOW } from '@translatr/utils';

describe('AuthGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AppFacade, useFactory: () => ({}) },
        { provide: WINDOW, useValue: undefined },
        { provide: LOGIN_URL, useValue: '' }
      ]
    });
  });

  it('should create', inject([AuthGuard], (guard: AuthGuard) => {
    expect(guard).toBeTruthy();
  }));
});
