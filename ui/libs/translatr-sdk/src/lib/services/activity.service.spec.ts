import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { LanguageProvider } from '@dev/translatr-sdk';

import { ActivityService } from './activity.service';

describe('ActivityService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [{ provide: HttpClient, useFactory: () => ({}) }, LanguageProvider]
    })
  );

  it('should be created', () => {
    const service: ActivityService = TestBed.get(ActivityService);
    expect(service).toBeTruthy();
  });
});
