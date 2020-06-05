import { inject, TestBed } from '@angular/core/testing';
import { AppFacade } from '../../../+state/app.facade';
import { ProjectGuard } from './project.guard';

describe('ProjectMemberGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProjectGuard, { provide: AppFacade, useFactory: () => ({}) }]
    });
  });

  it('should ...', inject([ProjectGuard], (guard: ProjectGuard) => {
    expect(guard).toBeTruthy();
  }));
});
