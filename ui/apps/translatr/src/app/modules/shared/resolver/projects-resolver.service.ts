import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { PagedList, Project } from '@dev/translatr-model';
import { ProjectService } from '@dev/translatr-sdk';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProjectsResolverService implements Resolve<PagedList<Project>> {
  constructor(private readonly projectService: ProjectService) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<PagedList<Project>> {
    return this.projectService.find();
  }
}
