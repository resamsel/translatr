import { TestBed } from '@angular/core/testing';

import { LocaleService } from './locale.service';
import { HttpClient } from '@angular/common/http';
import { ErrorHandler } from '@dev/translatr-sdk';

describe('LocaleService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      { provide: HttpClient, useFactory: () => ({}) },
      ErrorHandler
    ]
  }));

  it('should be created', () => {
    const service: LocaleService = TestBed.get(LocaleService);
    expect(service).toBeTruthy();
  });
});
