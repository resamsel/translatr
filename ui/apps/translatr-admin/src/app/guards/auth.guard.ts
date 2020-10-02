import { Inject, Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { User, UserRole } from '@dev/translatr-model';
import { LOGIN_URL, WINDOW } from '@translatr/utils';
import { Observable } from 'rxjs';
import { map, skip } from 'rxjs/operators';
import { AppFacade } from '../+state/app.facade';
import { environment } from '../../environments/environment';

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
    // TODO: Avoid doing this too often, i.e. have a loaded state for me$
    this.facade.loadMe();
    return this.facade.me$.pipe(
      skip(1),
      map((user: User) => {
        if (!user) {
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
          return false;
        }

        if (user.role !== UserRole.Admin) {
          this.router.navigate(['/forbidden'], {
            queryParams: {
              path: state.url
            }
          });
          return false;
        }

        return true;
      })
    );
  }
}
