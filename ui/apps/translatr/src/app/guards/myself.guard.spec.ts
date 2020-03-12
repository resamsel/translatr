import { inject, TestBed } from '@angular/core/testing';

import { MyselfGuard } from './myself.guard';
import { AppFacade } from '../+state/app.facade';
import { RouterTestingModule } from '@angular/router/testing';

describe('MyselfGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [
        MyselfGuard,
        { provide: AppFacade, useFactory: () => ({}) }
      ]
    });
  });

  it('should create', inject([MyselfGuard], (guard: MyselfGuard) => {
    expect(guard).toBeTruthy();
  }));
});
