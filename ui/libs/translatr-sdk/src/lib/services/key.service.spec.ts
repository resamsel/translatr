import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler, LanguageProvider } from '@dev/translatr-sdk';

import { KeyService } from './key.service';

describe('KeyService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider]
    })
  );

  it('should be created', () => {
    const service: KeyService = TestBed.get(KeyService);
    expect(service).toBeTruthy();
  });
});
