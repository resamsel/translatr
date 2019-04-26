import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Project } from '../../../../../../../libs/translatr-model/src/lib/model/project';
import { ProjectService } from '../../../../../../../libs/translatr-sdk/src/lib/services/project.service';
import { Observable } from 'rxjs';
import { PagedList } from '../../../../../../../libs/translatr-model/src/lib/model/paged-list';

@Injectable({
  providedIn: 'root'
})
export class ProjectsResolverService implements Resolve<PagedList<Project>> {

  constructor(
    private readonly projectService: ProjectService
  ) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<PagedList<Project>> {
    return this.projectService.find();
  }
}
