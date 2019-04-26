import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { UserService } from './user.service';
import { User } from '@dev/translatr-model';

@Injectable({
  providedIn: 'root'
})
export class AuthResolverService implements Resolve<User> {
  constructor(private readonly userService: UserService) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<User> {
    return this.userService.me();
  }
}
