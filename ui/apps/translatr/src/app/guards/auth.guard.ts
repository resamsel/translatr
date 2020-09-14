import { Inject, Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { LOGIN_URL, WINDOW } from '@translatr/utils';
import { Observable } from 'rxjs';
import { filter, map, tap } from 'rxjs/operators';
import { AppFacade } from '../+state/app.facade';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private readonly facade: AppFacade,
    private readonly router: Router,
    @Inject(WINDOW) private readonly window: Window,
    @Inject(LOGIN_URL) private readonly loginUrl: string
  ) {}

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
            if (this.loginUrl.startsWith('http://') || this.loginUrl.startsWith('https://')) {
              const url = new URL(this.loginUrl);
              url.searchParams.set('redirect_uri', state.url);
              this.window.location.href = url.toString();
            } else {
              this.router.navigate(['/login'], {
                queryParamsHandling: 'merge',
                queryParams: { redirect_uri: state.url }
              });
            }
            return false;
          } catch (e) {
            console.error(
              'Error while parsing login URL',
              this.loginUrl,
              this.window.location.href,
              e
            );
            return false;
          }
        }
      })
    );
  }
}
