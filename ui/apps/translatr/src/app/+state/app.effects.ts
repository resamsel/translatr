import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import {
  createProject,
  loadMe,
  loadProject,
  loadUsers,
  meLoaded,
  meLoadError,
  projectCreated,
  projectCreateError,
  projectLoaded,
  projectLoadError,
  projectUpdated,
  projectUpdateError,
  updateProject,
  usersLoaded,
  usersLoadError
} from './app.actions';
import { PagedList, Project, User } from '@dev/translatr-model';
import { switchMap } from 'rxjs/internal/operators/switchMap';
import { catchError, map } from 'rxjs/operators';
import { of } from 'rxjs/internal/observable/of';
import { ProjectService, UserService } from '@dev/translatr-sdk';

@Injectable()
export class AppEffects {
  loadMe$ = createEffect(() => this.actions$.pipe(
    ofType(loadMe),
    switchMap((action) => {
      return this.userService
        .me({fetch: 'featureFlags'})
        .pipe(map((user: User) => meLoaded({ payload: user })));
    }),
    catchError(error => of(meLoadError(error)))
  ));

  loadUsers$ = createEffect(() => this.actions$.pipe(
    ofType(loadUsers),
    switchMap((action) => {
      return this.userService
        .find(action.payload)
        .pipe(map((pagedList: PagedList<User>) =>
          usersLoaded({ payload: pagedList })));
    }),
    catchError(error => of(usersLoadError(error)))
  ));

  loadProject$ = createEffect(() => this.actions$.pipe(
    ofType(loadProject),
    switchMap((action) => {
        const payload = action.payload;
        return this.projectService
          .byOwnerAndName(
            payload.username,
            payload.projectName,
            { params: { fetch: 'myrole' } }
          )
          .pipe(
            map((p: Project) => projectLoaded({ payload: p })),
            catchError(error => of(projectLoadError({ error })))
          );
      }
    )
  ));

  createProject$ = createEffect(() => this.actions$.pipe(
    ofType(createProject),
    switchMap((action) =>
      this.projectService
        .create(action.payload)
        .pipe(
          map((payload: Project) => projectCreated({ payload })),
          catchError(error => of(projectCreateError({ error })))
        )
    )
  ));

  updateProject$ = createEffect(() => this.actions$.pipe(
    ofType(updateProject),
    switchMap((action) =>
      this.projectService
        .update(action.payload)
        .pipe(
          map((payload: Project) => projectUpdated({ payload })),
          catchError(error => of(projectUpdateError({ error })))
        )
    )
  ));

  constructor(
    private actions$: Actions,
    private readonly userService: UserService,
    private readonly projectService: ProjectService
  ) {
  }
}
