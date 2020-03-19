import { TestBed } from '@angular/core/testing';

import { KeyService } from './key.service';
import { HttpClient } from '@angular/common/http';
import { ErrorHandler } from '@dev/translatr-sdk';

describe('KeyService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      { provide: HttpClient, useFactory: () => ({}) },
      ErrorHandler
    ]
  }));

  it('should be created', () => {
    const service: KeyService = TestBed.get(KeyService);
    expect(service).toBeTruthy();
  });
});
