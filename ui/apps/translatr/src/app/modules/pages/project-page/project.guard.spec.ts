import { inject, TestBed } from '@angular/core/testing';

import { ProjectGuard } from './project.guard';

describe('ProjectMemberGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProjectGuard]
    });
  });

  it('should ...', inject([ProjectGuard], (guard: ProjectGuard) => {
    expect(guard).toBeTruthy();
  }));
});
