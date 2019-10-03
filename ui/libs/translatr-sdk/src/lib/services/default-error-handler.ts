import { Observable, throwError } from 'rxjs';
import { ErrorHandler } from './error-handler';
import { HttpErrorResponse } from '@angular/common/http';
import { NotificationService } from '@translatr/translatr-sdk/src/lib/services/notification.service';

const HTTP_STATUS_BAD_REQUEST = 400;
const HTTP_STATUS_UNAUTHORIZED = 401;
const HTTP_STATUS_FORBIDDEN = 403;

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
    private readonly notificationService: NotificationService,
    private readonly loginUrl: string
  ) {
    super();
  }

  handleError(err: HttpErrorResponse): Observable<never> {
    console.log('%s (HTTP code: %d, URL: %s)',
      errorMessage(err), err.status, err.url);

    switch (err.status) {
      case HTTP_STATUS_BAD_REQUEST:
        // Bad request, should be handled by UI code
        return throwError(err);
      case HTTP_STATUS_UNAUTHORIZED:
        const redirectUrl = `${this.loginUrl}?redirect_uri=${window.location.href}`;
        console.log('%s: redirecting to %s', err.statusText, redirectUrl);

        window.location.href = redirectUrl;

        return throwError(true);
      case HTTP_STATUS_FORBIDDEN:
        // Forbidden, should be handled by UI code
        // TODO: Handle forbidden (show message, redirect to forbidden page, ???)
      default:
        this.notificationService.notify(
          `Received error message: ${errorMessage(err)}`
        );

        return throwError(err);
    }
  }
}
