import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { ProjectPartialState } from './project.reducer';
import {
  deleteLocale,
  keysLoaded,
  loadKeys,
  loadLocales,
  loadMessages,
  loadProject,
  loadProjectActivities,
  loadProjectActivityAggregated,
  localeDeleted,
  localeDeleteError,
  localesLoaded,
  messagesLoaded,
  projectActivitiesLoaded,
  projectActivitiesLoadError,
  projectActivityAggregatedLoaded,
  projectActivityAggregatedLoadError,
  projectLoaded,
  projectLoadError,
  projectSaved,
  saveProject
} from './project.actions';
import { ActivityService, KeyService, LocaleService, MessageService, ProjectService } from '@dev/translatr-sdk';
import { catchError, map, switchMap, withLatestFrom } from 'rxjs/operators';
import { Activity, Aggregate, Key, Locale, Message, PagedList, Project } from '@dev/translatr-model';
import { of } from 'rxjs';
import { select, Store } from '@ngrx/store';
import { projectQuery } from './project.selectors';

@Injectable()
export class ProjectEffects {
  loadProject$ = createEffect(() => this.actions$.pipe(
    ofType(loadProject),
    switchMap((action) => {
        const payload = action.payload;
        return this.projectService
          .getProjectByOwnerAndName(payload.username, payload.projectName)
          .pipe(
            map((p: Project) => projectLoaded({ payload: p })),
            catchError(error => of(projectLoadError({ error })))
          );
      }
    )
  ));

  loadLocales$ = createEffect(() => this.actions$.pipe(
    ofType(loadLocales),
    withLatestFrom(this.store.pipe(select(projectQuery.getLocalesSearch))),
    switchMap(([action, localesSearch]) =>
      this.localeService
        .find({
          ...localesSearch,
          ...action.payload
        })
        .pipe(
          map((payload: PagedList<Locale>) => localesLoaded({ payload }))
        )
    )
  ));

  loadKeys$ = createEffect(() => this.actions$.pipe(
    ofType(loadKeys),
    withLatestFrom(this.store.pipe(select(projectQuery.getKeysSearch))),
    switchMap(([action, keysSearch]) =>
      this.keyService
        .find({
          ...keysSearch,
          ...action.payload
        })
        .pipe(map((payload: PagedList<Key>) => keysLoaded({ payload })))
    )
  ));

  loadMessages$ = createEffect(() => this.actions$.pipe(
    ofType(loadMessages),
    withLatestFrom(this.store.pipe(select(projectQuery.getMessagesSearch))),
    switchMap(([action, messagesSearch]) =>
      this.messageService
        .find({
          ...messagesSearch,
          ...action.payload
        })
        .pipe(map((payload: PagedList<Message>) => messagesLoaded({ payload })))
    )
  ));

  loadProjectActivity$ = createEffect(() => this.actions$.pipe(
    ofType(loadProjectActivityAggregated),
    switchMap((action) => {
      return this.activityService
          .aggregated({ projectId: action.payload.id })
          .pipe(
            map(
              (payload: PagedList<Aggregate>) =>
                projectActivityAggregatedLoaded({ payload })
            ),
            catchError(error =>
              of(projectActivityAggregatedLoadError({ error })))
          );
      }
    )
  ));

  loadProjectActivities$ = createEffect(() => this.actions$.pipe(
    ofType(loadProjectActivities),
    switchMap((action) => {
      return this.activityService
          .find(action.payload)
          .pipe(
            map((payload: PagedList<Activity>) =>
              projectActivitiesLoaded({ payload })),
            catchError(error =>
              of(projectActivitiesLoadError({ error })))
          );
      }
    )
  ));

  saveProject$ = createEffect(() => this.actions$.pipe(
    ofType(saveProject),
    switchMap((action) =>
      this.projectService
        .update(action.payload)
        .pipe(map((payload: Project) => projectSaved({ payload })))
    )
  ));

  // Locales

  deleteLocale$ = createEffect(() => this.actions$.pipe(
    ofType(deleteLocale),
    switchMap((action) =>
      this.localeService
        .delete(action.payload.id)
        .pipe(
          map((payload: Locale) => localeDeleted({ payload })),
          catchError(error => of(localeDeleteError({ error })))
        )
    )
  ));

  constructor(
    private readonly actions$: Actions,
    private readonly store: Store<ProjectPartialState>,
    private readonly projectService: ProjectService,
    private readonly localeService: LocaleService,
    private readonly keyService: KeyService,
    private readonly messageService: MessageService,
    private readonly activityService: ActivityService
  ) {
  }
}
