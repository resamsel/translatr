import { Inject, Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateChild,
  Router,
  RouterStateSnapshot
} from '@angular/router';
import { User, UserRole } from '@dev/translatr-model';
import { LOGIN_URL, WINDOW } from '@translatr/utils';
import { Observable } from 'rxjs';
import { catchError, map, take, timeout, withLatestFrom } from 'rxjs/operators';
import { AppFacade } from '../+state/app.facade';
import { environment } from '../../environments/environment';

/**
 * Route authorization for the admin app.
 *
 * This is a client-side UX boundary only: it stops non-admin users from
 * *rendering* admin pages, it is not a security control. Every admin API the
 * pages call MUST enforce the `Admin` role server-side; do not treat this guard
 * as access control.
 *
 * Applied as both `canActivate` and `canActivateChild` on the admin shell route
 * so that every current and future descendant page inherits the check without
 * declaring its own guard.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate, CanActivateChild {
  /** Give up waiting for the logged-in user to resolve after this long, and deny. */
  private static readonly RESOLVE_TIMEOUT_MS = 10000;

  constructor(
    private readonly facade: AppFacade,
    private readonly router: Router,
    @Inject(WINDOW) private readonly window: Window,
    @Inject(LOGIN_URL) private readonly loginUrl: string
  ) {}

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.checkAdmin(state);
  }

  canActivateChild(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.checkAdmin(state);
  }

  /**
   * Resolves to `true` only for an authenticated admin. Waits for the logged-in
   * user lookup to settle (loaded or failed) so the decision is deterministic,
   * and fails closed on error or timeout.
   */
  private checkAdmin(state: RouterStateSnapshot): Observable<boolean> {
    // TODO: Avoid doing this too often, i.e. have a loaded state for me$
    this.facade.loadMe();

    return this.facade.loggedInUserSettled$.pipe(
      take(1),
      timeout(AuthGuard.RESOLVE_TIMEOUT_MS),
      withLatestFrom(this.facade.me$),
      map(([, user]: [unknown, User | null | undefined]) => this.decide(user, state)),
      catchError(() =>
        // User state could not be resolved (load error or timeout): fail closed.
        // Never allow while unknown - /forbidden if a user is present, else login.
        this.facade.me$.pipe(
          take(1),
          map(user => {
            if (user) {
              this.redirectToForbidden(state);
            } else {
              this.redirectToLogin(state);
            }
            return false;
          })
        )
      )
    );
  }

  private decide(user: User | null | undefined, state: RouterStateSnapshot): boolean {
    if (!user) {
      this.redirectToLogin(state);
      return false;
    }

    if (user.role !== UserRole.Admin) {
      this.redirectToForbidden(state);
      return false;
    }

    return true;
  }

  private redirectToForbidden(state: RouterStateSnapshot): void {
    this.router.navigate(['/forbidden'], {
      queryParams: {
        path: state.url
      }
    });
  }

  private redirectToLogin(state: RouterStateSnapshot): void {
    if (this.loginUrl.startsWith('http://') || this.loginUrl.startsWith('https://')) {
      const url = new URL(this.loginUrl);
      url.searchParams.set('redirect_uri', environment.adminUrl + state.url);
      this.window.location.href = url.toString();
    } else {
      this.router.navigate([this.loginUrl], {
        queryParamsHandling: 'merge',
        queryParams: { redirect_uri: environment.adminUrl + state.url }
      });
    }
  }
}
