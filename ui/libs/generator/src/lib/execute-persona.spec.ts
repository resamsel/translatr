import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler } from '@dev/translatr-sdk';
import { Injector } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { LoadGeneratorConfig } from './load-generator-config';
import { Persona } from './personas/persona';
import { executePersona } from './utils';

class FailingPersona extends Persona {
  constructor() {
    super('Failing', {} as LoadGeneratorConfig, {} as Injector);
  }

  execute(): Observable<string> {
    return throwError(new HttpErrorResponse({ status: 401 }));
  }
}

describe('executePersona', () => {
  it('completes instead of propagating the error once retries are exhausted', done => {
    // given
    const persona = new FailingPersona();
    const errorHandler = new ErrorHandler();

    // when
    executePersona(persona, errorHandler, {
      maxRetryAttempts: 2,
      retryScalingDelay: 1
    }).subscribe({
      next: () => done.fail('should not emit a value'),
      error: () => done.fail('should not propagate the error to the caller'),
      complete: () => done()
    });
  });
});
