import { Inject, Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { AppFacade } from '../+state/app.facade';
import { map, tap } from 'rxjs/operators';
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
      map(x => !!x),
      tap((authenticated: boolean) => {
        if (!authenticated) {
          const url = new URL(this.loginUrl);
          // TODO: redirect to admin page!
          url.searchParams.set('redirect_uri', state.url);
          this.window.location.href = url.toString();
          return false;
        }
      })
    );
  }
}
