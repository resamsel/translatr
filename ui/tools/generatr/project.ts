import {Injector} from "@angular/core";
import {Observable, of} from "rxjs";
import {State} from "./state";
import {getRandomUser, getRandomUserAccessToken} from "./user";
import {AccessToken, PagedList, Project, ProjectService, User, UserRole, UserService} from "@dev/translatr-sdk";
import {catchError, map} from "rxjs/operators";
import {switchMap} from "rxjs/internal/operators/switchMap";
import * as randomName from 'random-name';
import {errorMessage, pickRandomly} from "./utils";
import {HttpErrorResponse} from "@angular/common/http";
import {filter} from "rxjs/internal/operators/filter";

const createProject = (project: Project, accessToken: AccessToken, projectService: ProjectService): Observable<Project> => {
  return projectService.create(project, {params: {access_token: accessToken.key}});
};

export const createRandomProject = (injector: Injector, state: State): Observable<State> => {
  // Randomly choose user, create project for that user with random name
  return getRandomUserAccessToken(injector, {limit: '10'}, (user: User) => user.role === UserRole.User)
    .pipe(
      filter((payload: { user: User, accessToken: AccessToken }) =>
        !!payload.user && !!payload.user.id && !!payload.accessToken && !!payload.accessToken.key),
      switchMap((payload: { user: User, accessToken: AccessToken }) =>
        createProject(
          {
            name: randomName.place().replace(' ', ''),
            ownerId: payload.user.id
          },
          payload.accessToken,
          injector.get(ProjectService))
          .pipe(map((project: Project) => ({user: payload.user, project})))
      ),
      map((payload: { user: User, project: Project }) =>
        ({...state, message: `${payload.user.name} created project ${payload.project.name}`})),
      catchError((err: HttpErrorResponse) => of({...state, message: errorMessage(err)}))
    );
};

export const updateRandomProject = (injector: Injector, state: State): Observable<State> => {
  const projectService = injector.get(ProjectService);
  // Randomly choose user, update project of that user randomly
  return getRandomUserAccessToken(injector, {limit: '10'}, (user: User) => user.role === UserRole.User)
    .pipe(
      filter((payload: { user: User, accessToken: AccessToken }) =>
        !!payload.user && !!payload.user.id && !!payload.accessToken && !!payload.accessToken.key),
      switchMap((payload: { user: User, accessToken: AccessToken }) =>
        projectService.find(
          {params: {owner: payload.user.id, access_token: payload.accessToken.key}}
        )
          .pipe(map((pagedList: PagedList<Project>) =>
            ({user: payload.user, project: pickRandomly(pagedList.list), accessToken: payload.accessToken})))
      ),
      switchMap((payload: { user: User, project: Project, accessToken: AccessToken }) => {
        if (!!payload.project) {
          return of(payload);
        }

        const project = {name: randomName.place().replace(' ', '')};
        return createProject(project, payload.accessToken, projectService)
          .pipe(map((project: Project) => ({user: payload.user, project})));
      }),
      switchMap((payload: { user: User, project: Project }) => {
        console.log('updating project...', payload.project.name);
        return projectService.update({
          ...payload.project,
          name: payload.project.name.endsWith('_')
            ? payload.project.name.replace('_', '')
            : `${payload.project.name}_`
        });
      }),
      map((project: Project) =>
        ({...state, message: `+++ ${project.ownerName} updated project ${project.name} +++`})),
      catchError((err: HttpErrorResponse) => of({...state, message: errorMessage(err)}))
    )
};

export const deleteRandomProject = (injector: Injector, state: State): Observable<State> => {
  // Randomly choose user, delete project of that user randomly
  return getRandomUser(injector.get(UserService), {limit: '10'}, (user: User) => user.role === UserRole.User)
    .pipe(map((user: User) => ({...state, message: `TODO: ${user.name} deleted random project`})));
};
