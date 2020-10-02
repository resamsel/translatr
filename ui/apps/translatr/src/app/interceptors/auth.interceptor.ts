import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { NavigationCancel, Router } from '@angular/router';
import { LOGIN_URL } from '@translatr/utils';
import { Observable, of } from 'rxjs';
import { catchError, filter, shareReplay } from 'rxjs/operators';

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

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private readonly navigationCancelled$: Observable<NavigationCancel> = this.router.events.pipe(
    filter<NavigationCancel>(event => event instanceof NavigationCancel),
    shareReplay(1)
  );

  constructor(
    private readonly router: Router,
    @Inject(LOGIN_URL) private readonly loginUrl: string
  ) {
    // this is necessary for the shareReplay to record the events
    this.navigationCancelled$.subscribe();
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((err: HttpErrorResponse) => {
        switch (err.status) {
          case HTTP_STATUS_BAD_REQUEST:
            // Bad request, should be handled by UI code
            break;

          case HTTP_STATUS_UNAUTHORIZED:
            // Unauthorized, redirecting to login page
            this.navigationCancelled$.subscribe(event =>
              this.router.navigate([this.loginUrl], {
                queryParamsHandling: 'merge',
                queryParams: { redirect_uri: `/ui${event.url}` }
              })
            );

            break;

          case HTTP_STATUS_FORBIDDEN:
            // Forbidden, redirecting to forbidden page
            this.router.navigate(['/forbidden'], {
              queryParams: {
                message: errorMessage(err)
              },
              skipLocationChange: true
            });

            break;

          case HTTP_STATUS_NOT_FOUND:
            // Not found, redirecting to not found page
            this.router.navigate(['/not-found'], {
              queryParams: {
                model: errorEntity(err),
                id: errorId(err)
              },
              skipLocationChange: true
            });

            break;
        }

        return of(err as any);
      })
    );
  }
}
