import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { ProjectFacade } from '../../shared/project-state';

@Injectable({
  providedIn: 'root'
})
export class ProjectAccessGuard implements CanActivate {
  constructor(private readonly facade: ProjectFacade) {}

  canActivate(
    _route: ActivatedRouteSnapshot,
    _state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.facade.canAccess$;
  }
}
