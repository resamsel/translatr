import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { PagedList, User } from '@dev/translatr-model';
import { UserService } from '@dev/translatr-sdk';

@Injectable({
  providedIn: 'root'
})
export class UsersResolverService implements Resolve<PagedList<User>> {
  constructor(private readonly userService: UserService) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<PagedList<User>> {
    return this.userService.find({ fetch: 'memberships' });
  }
}
