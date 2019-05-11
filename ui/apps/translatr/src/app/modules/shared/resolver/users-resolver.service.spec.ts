import { TestBed } from '@angular/core/testing';

import { ProjectsResolverService } from './projects-resolver.service';

describe('UserResolverService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ProjectsResolverService = TestBed.get(ProjectsResolverService);
    expect(service).toBeTruthy();
  });
});
