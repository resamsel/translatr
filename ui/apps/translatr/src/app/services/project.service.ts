import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {Project} from "../shared/project";
import {HttpClient, HttpParams} from "@angular/common/http";
import {map} from "rxjs/operators";
import {PagedList} from "../shared/paged-list";
import {convertTemporals, convertTemporalsList} from "../shared/mapper-utils";
import {Aggregate} from "../shared/aggregate";

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  constructor(private readonly http: HttpClient) {
  }

  getProjectByOwnerAndName(username: string, projectName: string, options?: {
    params?: HttpParams | {
      [param: string]: string | string[];
    }
  }): Observable<Project | undefined> {
    return this.http
      .get<Project>(`/api/${username}/${projectName}`, options)
      .pipe(
        map((project: Project) => ({
          ...convertTemporals(project),
          locales: convertTemporalsList(project.locales),
          keys: convertTemporalsList(project.keys)
        }))
      );
  }

  getProjects(options?: {
    params?: HttpParams | {
      [param: string]: string | string[];
    }
  }): Observable<PagedList<Project>> {
    return this.http
      .get<PagedList<Project>>('/api/projects', options)
      .pipe(
        map((list: PagedList<Project>) => ({
          ...list,
          list: convertTemporalsList(list.list)
        }))
      );
  }

  activity(projectId: string): Observable<PagedList<Aggregate>> {
    return this.http
      .get<PagedList<Aggregate>>(`/api/project/${projectId}/activity`);
  }

  create(project: { name: string }): Observable<Project> {
    return this.http
      .post<Project>('/api/project', project)
      .pipe(map(convertTemporals));
  }

  update(project: Project): Observable<Project> {
    return this.http
      .put<Project>('/api/project', project)
      .pipe(map(convertTemporals));
  }
}
