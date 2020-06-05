import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler } from '@dev/translatr-sdk';

import { ProjectService } from './project.service';

describe('ProjectService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler]
    })
  );

  it('should be created', () => {
    const service: ProjectService = TestBed.get(ProjectService);
    expect(service).toBeTruthy();
  });
});
