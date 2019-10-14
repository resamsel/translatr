import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { map, skip, tap } from 'rxjs/operators';
import { Project } from '@dev/translatr-model';
import { ProjectFacade } from './+state/project.facade';

@Injectable({
  providedIn: 'root'
})
export class ProjectGuard implements CanActivate {
  constructor(
    private readonly facade: ProjectFacade,
    private readonly router: Router
  ) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    this.facade.loadProject(route.params.username, route.params.projectName);
    return this.facade.project$.pipe(
      skip(1),
      map((project: Project) => !!project),
      tap((found: boolean) => {
        console.log('ProjectGuard: found?', found);
        if (!found) {
          this.router.navigate(
            ['/not-found'],
            {
              queryParams: {
                model: 'project',
                id: `${route.params.username}/${route.params.projectName}`
              }
            }
          );
        }
      })
    );
  }
}
