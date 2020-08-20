import { inject, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { LOGIN_URL, WINDOW } from '@translatr/utils';
import { AppFacade } from '../+state/app.facade';

import { AuthGuard } from './auth.guard';

describe('AuthGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
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
