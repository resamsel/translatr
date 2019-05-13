import { inject, TestBed } from '@angular/core/testing';

import { MyselfGuard } from './myself.guard';

describe('MyselfGuardGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MyselfGuard]
    });
  });

  it('should ...', inject([MyselfGuard], (guard: MyselfGuard) => {
    expect(guard).toBeTruthy();
  }));
});
