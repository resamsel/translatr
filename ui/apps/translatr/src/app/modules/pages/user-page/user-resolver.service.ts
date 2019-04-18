import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot} from "@angular/router";
import {EMPTY, Observable, of} from "rxjs";
import {mergeMap, take} from "rxjs/operators";
import {User} from "../../../../../../../libs/translatr-model/src/lib/model/user";
import {UserService} from "../../../../../../../libs/translatr-sdk/src/lib/services/user.service";

@Injectable({
  providedIn: 'root'
})
export class UserResolverService implements Resolve<User> {

  constructor(
    private readonly userService: UserService,
    private readonly router: Router
  ) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<User> {
    const username = route.paramMap.get('username');

    return this.userService
      .getUserByName(username, {params: {}})
      .pipe(
        take(1),
        mergeMap((user: User) => {
          if (user) {
            return of(user);
          }

          this.router.navigate(['/dashboard/users']);
          return EMPTY;
        })
      );
  }
}
