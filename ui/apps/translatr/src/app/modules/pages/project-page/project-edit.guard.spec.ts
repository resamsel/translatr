import { inject, TestBed } from '@angular/core/testing';
import { ProjectFacade } from './+state/project.facade';
import { ProjectEditGuard } from './project-edit.guard';

describe('ProjectEditGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProjectEditGuard, { provide: ProjectFacade, useFactory: () => ({}) }]
    });
  });

  it('should ...', inject([ProjectEditGuard], (guard: ProjectEditGuard) => {
    expect(guard).toBeTruthy();
  }));
});
