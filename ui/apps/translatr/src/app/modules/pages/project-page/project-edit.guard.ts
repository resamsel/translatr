import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { ProjectFacade } from './+state/project.facade';
import { take } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ProjectEditGuard implements CanActivate {
  constructor(private readonly facade: ProjectFacade) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.facade.canEdit$.pipe(take(1));
  }
}
