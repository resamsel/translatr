import { TestBed } from '@angular/core/testing';

import { MemberService } from './member.service';

describe('MemberService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: MemberService = TestBed.get(MemberService);
    expect(service).toBeTruthy();
  });
});
