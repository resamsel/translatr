import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
  UrlTree
} from '@angular/router';
import { User } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { map, skip, tap } from 'rxjs/operators';
import { UserFacade } from './+state/user.facade';

@Injectable()
export class UserGuard implements CanActivate {
  readonly excluded = ['login', 'register', 'dashboard'];

  constructor(private readonly facade: UserFacade, private readonly router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    if (this.excluded.includes(route.params.username)) {
      return true;
    }

    this.facade.loadUser(route.params.username);
    return this.facade.user$.pipe(
      skip(1),
      map((user: User) => !!user),
      tap((found: boolean) => {
        if (!found) {
          this.router.navigate(['/not-found'], {
            queryParams: {
              model: 'user',
              id: route.params.username
            }
          });
        }
      })
    );
  }
}
