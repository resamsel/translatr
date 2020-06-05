import { inject, TestBed } from '@angular/core/testing';
import { ProjectFacade } from './+state/project.facade';
import { ProjectAccessGuard } from './project-access.guard';

describe('ProjectMemberGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProjectAccessGuard, { provide: ProjectFacade, useFactory: () => ({}) }]
    });
  });

  it('should ...', inject([ProjectAccessGuard], (guard: ProjectAccessGuard) => {
    expect(guard).toBeTruthy();
  }));
});
