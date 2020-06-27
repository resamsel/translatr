import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Aggregate, Member, PagedList, Project, ProjectCriteria } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { convertTemporals, convertTemporalsList } from '../shared/mapper-utils';
import { AbstractService, encodePathParam } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { HttpHeader } from './http-header';
import { LanguageProvider } from './language-provider';

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
  constructor(http: HttpClient, errorHandler: ErrorHandler, languageProvider: LanguageProvider) {
    super(http, errorHandler, languageProvider, () => '/api/projects', '/api/project');
  }

  byOwnerAndName(
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
    const path = `/api/${encodePathParam(username)}/${encodePathParam(projectName)}`;
    return this.http
      .get<Project>(path, {
        headers: {
          [HttpHeader.AcceptLanguage]: this.languageProvider.getActiveLang()
        },
        ...options
      })
      .pipe(
        map(projectMapper),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'byOwnerAndName',
            params: [options],
            method: 'get',
            path
          })
        )
      );
  }

  activity(projectId: string): Observable<PagedList<Aggregate>> {
    const path = `/api/project/${projectId}/activity`;
    return this.http
      .get<PagedList<Aggregate>>(path, {
        headers: {
          [HttpHeader.AcceptLanguage]: this.languageProvider.getActiveLang()
        }
      })
      .pipe(
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'activity',
            params: [projectId],
            method: 'get',
            path
          })
        )
      );
  }

  addMember(member: Member): Observable<Member> {
    const path = `/api/project/${member.projectId}/members`;
    return this.http
      .post<Member>(path, member, {
        headers: {
          [HttpHeader.AcceptLanguage]: this.languageProvider.getActiveLang()
        }
      })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'addMember',
            params: [member],
            method: 'post',
            path
          })
        )
      );
  }

  updateMember(member: Member): Observable<Member> {
    const path = `/api/project/${member.projectId}/members`;
    return this.http
      .put<Member>(path, member, {
        headers: { [HttpHeader.AcceptLanguage]: this.languageProvider.getActiveLang() }
      })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'updateMember',
            params: [member],
            method: 'put',
            path
          })
        )
      );
  }
}
