import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {map} from 'rxjs/operators';
import {convertTemporals, convertTemporalsList} from '../shared/mapper-utils';
import {Aggregate, PagedList, Project, ProjectCriteria} from '@dev/translatr-model';
import {AbstractService} from './abstract.service';

const projectMapper = (project: Project) => ({
  ...convertTemporals(project),
  locales: !!project ? convertTemporalsList(project.locales) : undefined,
  keys: !!project ? convertTemporalsList(project.keys) : undefined,
  members: !!project ? convertTemporalsList(project.members) : undefined
});

@Injectable({
  providedIn: 'root'
})
export class ProjectService extends AbstractService<Project, ProjectCriteria> {

  constructor(http: HttpClient) {
    super(http, '/api/projects', '/api/project');
  }

  getProjectByOwnerAndName(username: string, projectName: string, options?: {
    params?: HttpParams | {
      [param: string]: string | string[];
    }
  }): Observable<Project | undefined> {
    return this.http
      .get<Project>(`/api/${username}/${projectName}`, options)
      .pipe(map(projectMapper));
  }

  getProjects(options?: ProjectCriteria): Observable<PagedList<Project>> {
    return this.find(options);
  }

  activity(projectId: string): Observable<PagedList<Aggregate>> {
    return this.http
      .get<PagedList<Aggregate>>(`/api/project/${projectId}/activity`);
  }
}
