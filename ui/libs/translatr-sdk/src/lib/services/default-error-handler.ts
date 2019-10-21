import { Observable, throwError } from 'rxjs';
import { ErrorHandler } from './error-handler';
import { HttpErrorResponse } from '@angular/common/http';
import { NotificationService } from '@translatr/translatr-sdk/src/lib/services/notification.service';
import { Router } from '@angular/router';

const HTTP_STATUS_BAD_REQUEST = 400;
const HTTP_STATUS_UNAUTHORIZED = 401;
const HTTP_STATUS_FORBIDDEN = 403;
const HTTP_STATUS_NOT_FOUND = 404;

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

const errorEntity = (err: HttpErrorResponse): string | undefined => {
  if (err.error.error !== undefined) {
    return err.error.error.entity;
  }

  return undefined;
};

const errorId = (err: HttpErrorResponse): string | undefined => {
  if (err.error.error !== undefined) {
    return err.error.error.id;
  }

  return undefined;
};

export class DefaultErrorHandler extends ErrorHandler {
  constructor(
    private readonly notificationService: NotificationService,
    private readonly router: Router,
    private readonly loginUrl: string
  ) {
    super();
  }

  handleError(err: HttpErrorResponse): Observable<never> {
    const message = errorMessage(err);
    console.log('%s (HTTP code: %d, URL: %s)',
      message, err.status, err.url);

    switch (err.status) {
      case HTTP_STATUS_BAD_REQUEST:
        // Bad request, should be handled by UI code
        return throwError(err);
      case HTTP_STATUS_UNAUTHORIZED:
        // Unauthorized, redirecting to login page
        const redirectUrl = `${this.loginUrl}?redirect_uri=${window.location.href}`;
        console.log('%s: redirecting to %s', err.statusText, redirectUrl);

        window.location.href = redirectUrl;

        return throwError(true);
      case HTTP_STATUS_FORBIDDEN:
        // Forbidden, redirecting to forbidden page
        this.router.navigate(['/forbidden'], {
          queryParams: {
            message
          },
          skipLocationChange: true
        });

        return throwError(true);
      case HTTP_STATUS_NOT_FOUND:
        // Not found, redirecting to not found page
        this.router.navigate(['/not-found'], {
          queryParams: {
            model: errorEntity(err),
            id: errorId(err)
          },
          skipLocationChange: true
        });

        return throwError(true);
      default:
        this.notificationService.notify(
          `Received error message: ${errorMessage(err)}`
        );

        return throwError(err);
    }
  }
}
