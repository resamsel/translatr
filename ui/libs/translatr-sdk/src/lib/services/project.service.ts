import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { convertTemporals, convertTemporalsList } from '../shared/mapper-utils';
import { Aggregate, Member, PagedList, Project, ProjectCriteria } from '@dev/translatr-model';
import { AbstractService } from './abstract.service';
import { ErrorHandler } from './error-handler';

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
    errorHandler: ErrorHandler
  ) {
    super(
      http,
      errorHandler,
      () => '/api/projects',
      '/api/project'
    );
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
      .pipe(
        map(projectMapper),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err))
      );
  }

  activity(projectId: string): Observable<PagedList<Aggregate>> {
    return this.http.get<PagedList<Aggregate>>(
      `/api/project/${projectId}/activity`
    ).pipe(
      catchError((err: HttpErrorResponse) =>
        this.errorHandler.handleError(err))
    );
  }

  addMember(member: Member): Observable<Member> {
    return this.http.post<Member>(
      `/api/project/${member.projectId}/members`,
      member
    )
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err))
      );
  }

  updateMember(member: Member): Observable<Member> {
    return this.http.put<Member>(
      `/api/project/${member.projectId}/members`,
      member
    )
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err))
      );
  }
}
