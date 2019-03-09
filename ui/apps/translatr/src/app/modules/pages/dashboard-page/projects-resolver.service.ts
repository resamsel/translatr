import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from "@angular/router";
import { Project } from "../../../shared/project";
import { ProjectService } from "../../../services/project.service";
import { Observable } from "rxjs";
import { PagedList } from "../../../shared/paged-list";

@Injectable({
  providedIn: 'root'
})
export class ProjectsResolverService implements Resolve<PagedList<Project>> {

  constructor(
    private readonly projectService: ProjectService
  ) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<PagedList<Project>> {
    return this.projectService.getProjects();
  }
}
