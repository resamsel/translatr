import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler } from '@dev/translatr-sdk';

import { LocaleService } from './locale.service';

describe('LocaleService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler]
    })
  );

  it('should be created', () => {
    const service: LocaleService = TestBed.get(LocaleService);
    expect(service).toBeTruthy();
  });
});
