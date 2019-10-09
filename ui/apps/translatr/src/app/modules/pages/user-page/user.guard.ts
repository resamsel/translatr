import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { UserFacade } from './+state/user.facade';
import { map, skip, tap } from 'rxjs/operators';
import { User } from '@dev/translatr-model';

@Injectable()
export class UserGuard implements CanActivate {
  constructor(
    private readonly facade: UserFacade,
    private readonly router: Router
  ) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    this.facade.loadUser(route.params.username);
    return this.facade.user$.pipe(
      skip(1),
      map((user: User) => !!user),
      tap((found: boolean) => {
        console.log('UserGuard: found?', found);
        if (!found) {
          this.router.navigate(
            ['/not-found'],
            {
              queryParams: {
                model: 'user',
                id: route.params.username
              }
            }
          );
        }
      })
    );
  }
}
