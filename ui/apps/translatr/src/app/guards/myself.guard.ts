import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { User, UserRole } from '@dev/translatr-model';
import { findParam } from '@translatr/utils';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppFacade } from '../+state/app.facade';

@Injectable({
  providedIn: 'root'
})
export class MyselfGuard implements CanActivate {
  constructor(private readonly appFacade: AppFacade, private readonly router: Router) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    return this.appFacade.me$.pipe(
      map((me: User) => {
        if (!me) {
          this.router.navigate(['/']);
          return false;
        }

        const username = findParam(next, 'username');
        if (me.role !== UserRole.Admin && me.username !== username) {
          if (next.data !== undefined && !next.data.redirect) {
            return false;
          }

          if (next.data !== undefined && next.data.redirectUri) {
            this.router.navigate(next.data.redirectUri);
          } else if (username !== undefined) {
            this.router.navigate(['/', username]);
          } else {
            this.router.navigate(['/dashboard']);
          }

          return false;
        }

        return true;
      })
    );
  }
}
