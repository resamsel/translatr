import {
  HttpContextToken,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { isSameOriginApiRequest } from './same-origin';

/**
 * Carries a per-request access token. `AbstractService.withAuth(token)` sets
 * this on an `HttpContext`; `AccessTokenInterceptor` turns it into an
 * `?access_token=` query parameter. Default `undefined` means "unauthenticated
 * request" — the shared DI instance never sets it.
 */
export const ACCESS_TOKEN = new HttpContextToken<string | undefined>(() => undefined);

/**
 * Appends `access_token` to the query string of a same-origin API request when
 * the request context carries one (see {@link ACCESS_TOKEN}). Replaces the old
 * `create`/`update`/`delete` `{ params: { access_token } }` option.
 */
@Injectable()
export class AccessTokenInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const accessToken = request.context.get(ACCESS_TOKEN);

    if (!accessToken || !isSameOriginApiRequest(request.url)) {
      return next.handle(request);
    }

    return next.handle(request.clone({ params: request.params.set('access_token', accessToken) }));
  }
}
