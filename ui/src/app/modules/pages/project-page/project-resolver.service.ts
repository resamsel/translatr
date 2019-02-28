import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot} from "@angular/router";
import {Project} from "../../../shared/project";
import {ProjectService} from "../../../services/project.service";
import {EMPTY, Observable, of} from "rxjs";
import {mergeMap, take} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class ProjectResolverService implements Resolve<Project> {

  constructor(
    private readonly projectService: ProjectService,
    private readonly router: Router
  ) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Project> {
    const id = route.paramMap.get('id');

    console.log('resolver', id);

    return this.projectService
      .getProject(id, {params: {fetch: 'keys,locales'}})
      .pipe(
        take(1),
        mergeMap((project: Project) => {
          if (project) {
            return of(project);
          }

          this.router.navigate(['/projects']);
          return EMPTY;
        })
      );
  }
}
