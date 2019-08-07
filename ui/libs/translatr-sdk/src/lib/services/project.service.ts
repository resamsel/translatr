import { Inject, Injectable, Optional } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { convertTemporals, convertTemporalsList } from '../shared/mapper-utils';
import { Aggregate, Member, PagedList, Project, ProjectCriteria } from '@dev/translatr-model';
import { AbstractService } from './abstract.service';
import { Router } from '@angular/router';
import { LOGIN_URL } from '@translatr/utils';

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
  constructor(
    http: HttpClient,
    router?: Router,
    @Inject(LOGIN_URL) @Optional() loginUrl?: string
  ) {
    super(http, router, loginUrl, () => '/api/projects', '/api/project');
  }

  getProjectByOwnerAndName(
    username: string,
    projectName: string,
    options?: {
      params?:
        | HttpParams
        | {
        [param: string]: string | string[];
      };
    }
  ): Observable<Project | undefined> {
    return this.http
      .get<Project>(`/api/${username}/${projectName}`, options)
      .pipe(map(projectMapper));
  }

  activity(projectId: string): Observable<PagedList<Aggregate>> {
    return this.http.get<PagedList<Aggregate>>(
      `/api/project/${projectId}/activity`
    );
  }

  addMember(member: Member): Observable<Member> {
    return this.http.post<Member>(
      `/api/project/${member.projectId}/members`,
      member
    )
      .pipe(map(convertTemporals));
  }

  updateMember(member: Member): Observable<Member> {
    return this.http.put<Member>(
      `/api/project/${member.projectId}/members`,
      member
    )
      .pipe(map(convertTemporals));
  }
}
