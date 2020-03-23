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
          try {
            const url = new URL(this.loginUrl);
            url.searchParams.set('redirect_uri', state.url);
            this.window.location.href = url.toString();
            return false;
          } catch (e) {
            console.log('Error while parsing login URL', this.loginUrl, e);
            return false;
          }
        }
      })
    );
  }
}
