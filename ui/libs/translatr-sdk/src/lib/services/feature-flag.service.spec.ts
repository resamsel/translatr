import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ErrorHandler, FeatureFlagService, LanguageProvider } from '@dev/translatr-sdk';

describe('FeatureFlagService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, ErrorHandler, LanguageProvider]
    })
  );

  it('should be created', () => {
    const service: FeatureFlagService = TestBed.get(FeatureFlagService);
    expect(service).toBeTruthy();
  });
});
