import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpHeader } from './http-header';
import { LanguageProvider } from './language-provider';
import { isSameOriginApiRequest } from './same-origin';

/**
 * Adds `Accept-Language` (the active UI language) to every request that targets
 * this app's own API. Same-origin only: relative URLs, and absolute URLs that
 * point at the page's own origin. Third-party requests (auth-provider redirects,
 * CDNs) are left untouched.
 *
 * This replaces the per-call `Accept-Language` header that `AbstractService` and
 * the resource services used to set by hand, so requests issued through the
 * generated API clients carry it too.
 */
@Injectable()
export class AcceptLanguageInterceptor implements HttpInterceptor {
  constructor(private readonly languageProvider: LanguageProvider) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!isSameOriginApiRequest(request.url)) {
      return next.handle(request);
    }

    return next.handle(
      request.clone({
        headers: request.headers.set(
          HttpHeader.AcceptLanguage,
          this.languageProvider.getActiveLang(),
        ),
      }),
    );
  }
}
