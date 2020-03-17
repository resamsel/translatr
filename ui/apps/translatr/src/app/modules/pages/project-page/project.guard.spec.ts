import { inject, TestBed } from '@angular/core/testing';
import { ProjectGuard } from './project.guard';
import { AppFacade } from '../../../+state/app.facade';

describe('ProjectMemberGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProjectGuard,
        { provide: AppFacade, useFactory: () => ({}) }
      ]
    });
  });

  it('should ...', inject([ProjectGuard], (guard: ProjectGuard) => {
    expect(guard).toBeTruthy();
  }));
});
