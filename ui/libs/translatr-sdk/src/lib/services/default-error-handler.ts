import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { ErrorHandler } from './error-handler';
import { HttpErrorResponse } from '@angular/common/http';

const errorMessage = (err: HttpErrorResponse): string => {
  let type = 'Error';
  let message = err.message;

  if (err.error.error !== undefined) {
    const error = err.error.error;
    if (error.type !== undefined) {
      type = error.type;
    }
    if (error.message !== undefined) {
      message = error.message;
    }
  }

  return `${type}: "${message}"`;
};

export class DefaultErrorHandler extends ErrorHandler {
  constructor(
    private readonly router: Router,
    private readonly loginUrl: string
  ) {
    super();
  }

  handleError(err: HttpErrorResponse): Observable<never> {
    console.log('%s (HTTP code: %d, URL: %s)',
      errorMessage(err), err.status, err.url);

    if (err.status >= 400) {
      if (this.router !== undefined
        && this.loginUrl !== undefined
        && this.loginUrl !== null) {
        console.log('%s: redirecting to %s', err.statusText, this.loginUrl);
        this.router.navigate([this.loginUrl])
          .then((navigated: boolean) => {
            if (!navigated) {
              window.location.href = `${this.loginUrl}?redirect_uri=${window.location.href}`;
            }
          });
      }

      return throwError(true);
    }

    return throwError(err);
  }
}
