import { Injectable } from '@angular/core';
import { PagedList, Project, User } from '@dev/translatr-model';
import { ProjectService, UserService } from '@dev/translatr-sdk';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { catchError, map, switchMap, withLatestFrom } from 'rxjs/operators';
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
  updatePreferredLanguage,
  updateProject,
  updateSettings,
  usersLoaded,
  usersLoadError
} from './app.actions';
import { AppState } from './app.reducer';
import { appQuery } from './app.selectors';

@Injectable()
export class AppEffects {
  loadMe$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadMe),
      switchMap(() =>
        this.userService
          .me({ fetch: 'features' })
          .pipe(map((user: User) => meLoaded({ payload: user })))
      ),
      catchError(error => of(meLoadError(error)))
    )
  );

  updatePreferredLanguage$ = createEffect(() =>
    this.actions$.pipe(
      ofType(updatePreferredLanguage),
      withLatestFrom(this.store.select(appQuery.getMe)),
      switchMap(([action, me]) =>
        this.userService
          .update({ id: me.id, preferredLanguage: action.payload })
          .pipe(map((user: User) => meLoaded({ payload: user })))
      ),
      catchError(error => of(meLoadError(error)))
    )
  );

  updateSettings$ = createEffect(() =>
    this.actions$.pipe(
      ofType(updateSettings),
      withLatestFrom(this.store.select(appQuery.getMe)),
      switchMap(([action, me]) =>
        this.userService
          .updateSettings(me.id, action.payload)
          .pipe(map((user: User) => meLoaded({ payload: user })))
      ),
      catchError(error => of(meLoadError(error)))
    )
  );

  loadUsers$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadUsers),
      switchMap(action => {
        return this.userService
          .find(action.payload)
          .pipe(map((pagedList: PagedList<User>) => usersLoaded({ payload: pagedList })));
      }),
      catchError(error => of(usersLoadError(error)))
    )
  );

  loadProject$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadProject),
      switchMap(action => {
        const payload = action.payload;
        return this.projectService
          .byOwnerAndName(payload.username, payload.projectName, { params: { fetch: 'myrole' } })
          .pipe(
            map((p: Project) => projectLoaded({ payload: p })),
            catchError(error => of(projectLoadError({ error })))
          );
      })
    )
  );

  createProject$ = createEffect(() =>
    this.actions$.pipe(
      ofType(createProject),
      switchMap(action =>
        this.projectService.create(action.payload).pipe(
          map((payload: Project) => projectCreated({ payload })),
          catchError(error => of(projectCreateError({ error })))
        )
      )
    )
  );

  updateProject$ = createEffect(() =>
    this.actions$.pipe(
      ofType(updateProject),
      switchMap(action =>
        this.projectService.update(action.payload).pipe(
          map((payload: Project) => projectUpdated({ payload })),
          catchError(error => of(projectUpdateError({ error })))
        )
      )
    )
  );

  constructor(
    private readonly actions$: Actions,
    private readonly store: Store<AppState>,
    private readonly userService: UserService,
    private readonly projectService: ProjectService
  ) {}
}
