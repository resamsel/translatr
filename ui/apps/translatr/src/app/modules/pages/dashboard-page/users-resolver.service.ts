import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from "@angular/router";
import { Observable } from "rxjs";
import { PagedList } from "../../../shared/paged-list";
import { User } from "../../../shared/user";
import { UserService } from "../../../services/user.service";

@Injectable({
  providedIn: 'root'
})
export class UsersResolverService implements Resolve<PagedList<User>> {

  constructor(
    private readonly userService: UserService
  ) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<PagedList<User>> {
    return this.userService.getUsers({params: {fetch: 'memberships'}});
  }
}
