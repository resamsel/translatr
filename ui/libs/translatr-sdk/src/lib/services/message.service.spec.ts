import { TestBed } from '@angular/core/testing';

import { MessageService } from './message.service';
import { HttpClient } from '@angular/common/http';
import { ErrorHandler } from '@dev/translatr-sdk';

describe('MessageService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      { provide: HttpClient, useFactory: () => ({}) },
      ErrorHandler
    ]
  }));

  it('should be created', () => {
    const service: MessageService = TestBed.get(MessageService);
    expect(service).toBeTruthy();
  });
});
