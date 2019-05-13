import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap, take } from 'rxjs/operators';
import { User } from '@dev/translatr-model';
import { UserService } from '@dev/translatr-sdk';

@Injectable({
  providedIn: 'root'
})
export class UserResolverService implements Resolve<User> {
  constructor(
    private readonly userService: UserService,
    private readonly router: Router
  ) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<User> {
    const username = route.paramMap.get('username');

    return this.userService.byUsername(username, { params: {} }).pipe(
      take(1),
      mergeMap((user: User) => {
        if (user) {
          return of(user);
        }

        this.router.navigate(['/users']);
        return EMPTY;
      })
    );
  }
}
