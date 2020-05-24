import { Inject, Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { AppFacade } from '../+state/app.facade';
import { filter, map, tap } from 'rxjs/operators';
import { LOGIN_URL, WINDOW } from '@translatr/utils';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private readonly facade: AppFacade,
    @Inject(WINDOW) private readonly window: Window,
    @Inject(LOGIN_URL) private readonly loginUrl: string
  ) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    return this.facade.me$.pipe(
      filter(x => x !== undefined), // skip default value
      map(x => x !== null), // null means unauthenticated
      tap((authenticated: boolean) => {
        if (!authenticated) {
          try {
            let url;
            if (this.loginUrl.startsWith('http://') || this.loginUrl.startsWith('https://')) {
              url = new URL(this.loginUrl);
            } else {
              url = new URL(this.window.location.href);
              url.pathname = this.loginUrl;
            }
            url.searchParams.set('redirect_uri', state.url);
            this.window.location.href = url.toString();
            return false;
          } catch (e) {
            console.error('Error while parsing login URL', this.loginUrl, this.window.location.href, e);
            return false;
          }
        }
      })
    );
  }
}
