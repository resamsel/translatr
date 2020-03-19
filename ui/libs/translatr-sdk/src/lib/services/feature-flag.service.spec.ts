import { TestBed } from '@angular/core/testing';

import { HttpClient } from '@angular/common/http';
import { ErrorHandler, FeatureFlagService } from '@dev/translatr-sdk';

describe('FeatureFlagService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      { provide: HttpClient, useFactory: () => ({}) },
      ErrorHandler
    ]
  }));

  it('should be created', () => {
    const service: FeatureFlagService = TestBed.get(FeatureFlagService);
    expect(service).toBeTruthy();
  });
});
