import { inject, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppFacade } from '../+state/app.facade';

import { MyselfGuard } from './myself.guard';

describe('MyselfGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [MyselfGuard, { provide: AppFacade, useFactory: () => ({}) }]
    });
  });

  it('should create', inject([MyselfGuard], (guard: MyselfGuard) => {
    expect(guard).toBeTruthy();
  }));
});
