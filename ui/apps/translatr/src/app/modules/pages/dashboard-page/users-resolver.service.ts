import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from "@angular/router";
import { Observable } from "rxjs";
import { PagedList } from "../../../../../../../libs/translatr-model/src/lib/model/paged-list";
import { User } from "../../../../../../../libs/translatr-model/src/lib/model/user";
import { UserService } from "../../../../../../../libs/translatr-sdk/src/lib/services/user.service";

@Injectable({
  providedIn: 'root'
})
export class UsersResolverService implements Resolve<PagedList<User>> {

  constructor(
    private readonly userService: UserService
  ) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<PagedList<User>> {
    return this.userService.getUsers({fetch: 'memberships'});
  }
}
