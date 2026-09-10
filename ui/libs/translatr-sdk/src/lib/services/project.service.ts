import { HttpClient, HttpContext, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Aggregate, Member, PagedList, Project, ProjectCriteria } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { convertTemporals, convertTemporalsList } from '../shared/mapper-utils';
import { ProjectsService } from '../generated/api/projects.service';
import { ProjectDto } from '../generated/model/projectDto';
import { AbstractService, PagedListLike, encodePathParam } from './abstract.service';
import { ErrorHandler } from './error-handler';
import { LanguageProvider } from './language-provider';

const projectMapper = (project: Project) => ({
  ...convertTemporals(project),
  members: project ? convertTemporalsList(project.members) : undefined,
});

@Injectable({
  providedIn: 'root',
})
export class ProjectService extends AbstractService<Project, ProjectCriteria> {
  constructor(
    http: HttpClient,
    errorHandler: ErrorHandler,
    languageProvider: LanguageProvider,
    client: ProjectsService,
  ) {
    super(http, errorHandler, languageProvider, {
      entityPath: '/api/project',
      listPath: () => '/api/projects',
      // ProjectCriteria.owner maps to the generated `ownerUsername` param; the
      // generated `name` param has no criteria field. See notes.md.
      list: (c: ProjectCriteria | undefined, context?: HttpContext) =>
        client.findProjects(
          c?.search,
          c?.offset,
          c?.limit,
          c?.order,
          c?.fetch,
          c?.ownerId,
          c?.owner,
          c?.memberId,
          undefined,
          'body',
          false,
          { context },
        ) as unknown as Observable<PagedListLike<Project>>,
      get: (id: string | number, context?: HttpContext) =>
        client.getProject(String(id), 'body', false, { context }) as unknown as Observable<Project>,
      create: (dto: Project, context?: HttpContext) =>
        client.createProject(dto as unknown as ProjectDto, 'body', false, {
          context,
        }) as unknown as Observable<Project>,
      update: (dto: Partial<Project>, context?: HttpContext) =>
        client.updateProject(dto as unknown as ProjectDto, 'body', false, {
          context,
        }) as unknown as Observable<Project>,
      delete: (id: string | number, context?: HttpContext) =>
        client.deleteProject(String(id), 'body', false, {
          context,
        }) as unknown as Observable<Project>,
    });
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
    },
  ): Observable<Project | undefined> {
    const path = `/api/${encodePathParam(username)}/${encodePathParam(projectName)}`;
    return this.http
      .get<Project>(path, {
        context: this.authContext,
        ...options,
      })
      .pipe(
        map(projectMapper),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'byOwnerAndName',
            params: [options],
            method: 'get',
            path,
          }),
        ),
      );
  }

  activity(projectId: string): Observable<PagedList<Aggregate>> {
    const path = `/api/project/${projectId}/activity`;
    return this.http
      .get<PagedList<Aggregate>>(path, {
        context: this.authContext,
      })
      .pipe(
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'activity',
            params: [projectId],
            method: 'get',
            path,
          }),
        ),
      );
  }

  addMember(member: Member): Observable<Member> {
    const path = `/api/project/${member.projectId}/members`;
    return this.http
      .post<Member>(path, member, {
        context: this.authContext,
      })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'addMember',
            params: [member],
            method: 'post',
            path,
          }),
        ),
      );
  }

  updateMember(member: Member): Observable<Member> {
    const path = `/api/project/${member.projectId}/members`;
    return this.http
      .put<Member>(path, member, {
        context: this.authContext,
      })
      .pipe(
        map(convertTemporals),
        catchError((err: HttpErrorResponse) =>
          this.errorHandler.handleError(err, {
            name: 'updateMember',
            params: [member],
            method: 'put',
            path,
          }),
        ),
      );
  }
}
