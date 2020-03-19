import { TestBed } from '@angular/core/testing';

import { MemberService } from './member.service';
import { HttpClient } from '@angular/common/http';
import { ErrorHandler } from '@dev/translatr-sdk';

describe('MemberService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      { provide: HttpClient, useFactory: () => ({}) },
      ErrorHandler
    ]
  }));

  it('should be created', () => {
    const service: MemberService = TestBed.get(MemberService);
    expect(service).toBeTruthy();
  });
});
