import {Injector} from "@angular/core";
import {Observable, of} from "rxjs";
import {State} from "./state";
import {getRandomUser, getRandomUserAccessToken} from "./user";
import {AccessToken, Project, ProjectService, User, UserRole, UserService} from "@dev/translatr-sdk";
import {catchError, map} from "rxjs/operators";
import {switchMap} from "rxjs/internal/operators/switchMap";
import * as randomName from 'random-name';
import {errorMessage} from "./utils";
import {HttpErrorResponse} from "@angular/common/http";

export const createRandomProject = (injector: Injector, state: State): Observable<State> => {
  // Randomly choose user, create project for that user with random name
  return getRandomUserAccessToken(injector, {limit: '10'}, (user: User) => user.role === UserRole.User)
    .pipe(
      switchMap((payload: {user: User, accessToken: AccessToken}) => injector.get(ProjectService)
        .create(
          {
            name: randomName.place().replace(' ', ''),
            ownerId: payload.user.id
          },
          {params: {access_token: payload.accessToken.key}}
        )
        .pipe(map((project: Project) => ({user: payload.user, project})))
      ),
      map((payload: {user: User, project: Project}) =>
        ({...state, message: `${payload.user.name} created project ${payload.project.name}`})),
      catchError((err: HttpErrorResponse) => of({...state, message: errorMessage(err)}))
    );
};

export const updateRandomProject = (injector: Injector, state: State): Observable<State> => {
  // Randomly choose user, update project of that user randomly
  return getRandomUser(injector.get(UserService), {limit: '10'}, (user: User) => user.role === UserRole.User)
    .pipe(map((user: User) => ({...state, message: `${user.name} updated random project`})));
};

export const deleteRandomProject = (injector: Injector, state: State): Observable<State> => {
  // Randomly choose user, delete project of that user randomly
  return getRandomUser(injector.get(UserService), {limit: '10'}, (user: User) => user.role === UserRole.User)
    .pipe(map((user: User) => ({...state, message: `${user.name} deleted random project`})));
};
