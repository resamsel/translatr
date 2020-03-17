import { inject, TestBed } from '@angular/core/testing';
import { ProjectEditGuard } from './project-edit.guard';
import { ProjectFacade } from './+state/project.facade';

describe('ProjectEditGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProjectEditGuard,
        { provide: ProjectFacade, useFactory: () => ({}) }
      ]
    });
  });

  it('should ...', inject([ProjectEditGuard], (guard: ProjectEditGuard) => {
    expect(guard).toBeTruthy();
  }));
});
