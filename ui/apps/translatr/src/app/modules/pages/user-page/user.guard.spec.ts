import { inject, TestBed } from '@angular/core/testing';

import { UserGuard } from './user.guard';
import { RouterTestingModule } from '@angular/router/testing';
import { UserFacade } from './+state/user.facade';

describe('UserGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserGuard,
        { provide: UserFacade, useFactory: () => ({}) }
      ],
      imports: [RouterTestingModule]
    });
  });

  it('should ...', inject([UserGuard], (guard: UserGuard) => {
    expect(guard).toBeTruthy();
  }));
});
