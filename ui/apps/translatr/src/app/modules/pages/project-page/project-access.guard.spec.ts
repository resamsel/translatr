import { inject, TestBed } from '@angular/core/testing';

import { ProjectAccessGuard } from './project-access.guard';

describe('ProjectMemberGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProjectAccessGuard]
    });
  });

  it('should ...', inject([ProjectAccessGuard], (guard: ProjectAccessGuard) => {
    expect(guard).toBeTruthy();
  }));
});
