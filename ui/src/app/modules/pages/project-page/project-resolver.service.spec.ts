import { TestBed } from '@angular/core/testing';

import { ProjectResolverService } from './project-resolver.service';

describe('ProjectResolverService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ProjectResolverService = TestBed.get(ProjectResolverService);
    expect(service).toBeTruthy();
  });
});
