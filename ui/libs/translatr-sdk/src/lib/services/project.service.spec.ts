import { TestBed } from '@angular/core/testing';

import { ProjectService } from './project.service';
import { HttpClient } from '@angular/common/http';
import { ErrorHandler } from '@dev/translatr-sdk';

describe('ProjectService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      { provide: HttpClient, useFactory: () => ({}) },
      ErrorHandler
    ]
  }));

  it('should be created', () => {
    const service: ProjectService = TestBed.get(ProjectService);
    expect(service).toBeTruthy();
  });
});
