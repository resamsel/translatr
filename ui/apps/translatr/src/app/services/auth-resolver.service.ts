import { Injectable } from '@angular/core';
import { User } from "../shared/user";
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from "@angular/router";
import { Observable } from "rxjs";
import { UserService } from "./user.service";

@Injectable({
  providedIn: 'root'
})
export class AuthResolverService implements Resolve<User> {
  constructor(private readonly userService: UserService) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<User> {
    return this.userService.getLoggedInUser();
  }
}
