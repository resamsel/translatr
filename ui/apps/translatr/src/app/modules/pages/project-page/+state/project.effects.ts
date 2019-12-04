import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { ProjectPartialState } from './project.reducer';
import {
  createKey,
  createLocale,
  deleteKey,
  deleteLocale,
  deleteMember,
  keyCreated,
  keyCreateError,
  keyDeleted,
  keyDeleteError,
  keysLoaded,
  keyUpdated,
  keyUpdateError,
  loadKeys,
  loadLocales,
  loadMessages,
  loadProject,
  loadProjectActivities,
  loadProjectActivityAggregated,
  localeCreated,
  localeCreateError,
  localeDeleted,
  localeDeleteError,
  localesLoaded,
  localeUpdated,
  localeUpdateError,
  memberDeleted,
  memberDeleteError,
  messagesLoaded,
  projectActivitiesLoaded,
  projectActivitiesLoadError,
  projectActivityAggregatedLoaded,
  projectActivityAggregatedLoadError,
  projectLoaded,
  projectLoadError,
  projectSaved,
  saveProject,
  updateKey,
  updateLocale
} from './project.actions';
import { ActivityService, KeyService, LocaleService, MessageService, ProjectService } from '@dev/translatr-sdk';
import { catchError, map, switchMap, withLatestFrom } from 'rxjs/operators';
import { Activity, Aggregate, Key, Locale, Member, Message, PagedList, Project } from '@dev/translatr-model';
import { of } from 'rxjs';
import { select, Store } from '@ngrx/store';
import { projectQuery } from './project.selectors';
import { MemberService } from '@translatr/translatr-sdk/src/lib/services/member.service';

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
    switchMap((action) =>
      this.activityService
        .aggregated({ projectId: action.payload.id })
        .pipe(
          map(
            (payload: PagedList<Aggregate>) =>
              projectActivityAggregatedLoaded({ payload })
          ),
          catchError(error =>
            of(projectActivityAggregatedLoadError({ error })))
        )
    )
  ));

  loadProjectActivities$ = createEffect(() => this.actions$.pipe(
    ofType(loadProjectActivities),
    switchMap((action) =>
      this.activityService
        .find(action.payload)
        .pipe(
          map((payload: PagedList<Activity>) =>
            projectActivitiesLoaded({ payload })),
          catchError(error =>
            of(projectActivitiesLoadError({ error })))
        )
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

  createLocale$ = createEffect(() => this.actions$.pipe(
    ofType(createLocale),
    switchMap((action) =>
      this.localeService
        .create(action.payload)
        .pipe(
          map((payload: Locale) => localeCreated({ payload })),
          catchError(error => of(localeCreateError({ error })))
        )
    )
  ));

  updateLocale$ = createEffect(() => this.actions$.pipe(
    ofType(updateLocale),
    switchMap((action) =>
      this.localeService
        .update(action.payload)
        .pipe(
          map((payload: Locale) => localeUpdated({ payload })),
          catchError(error => of(localeUpdateError({ error })))
        )
    )
  ));

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

  // Keys

  createKey$ = createEffect(() => this.actions$.pipe(
    ofType(createKey),
    switchMap((action) =>
      this.keyService
        .create(action.payload)
        .pipe(
          map((payload: Key) => keyCreated({ payload })),
          catchError(error => of(keyCreateError({ error })))
        )
    )
  ));

  updateKey$ = createEffect(() => this.actions$.pipe(
    ofType(updateKey),
    switchMap((action) =>
      this.keyService
        .update(action.payload)
        .pipe(
          map((payload: Key) => keyUpdated({ payload })),
          catchError(error => of(keyUpdateError({ error })))
        )
    )
  ));

  deleteKey$ = createEffect(() => this.actions$.pipe(
    ofType(deleteKey),
    switchMap((action) =>
      this.keyService
        .delete(action.payload.id)
        .pipe(
          map((payload: Key) => keyDeleted({ payload })),
          catchError(error => of(keyDeleteError({ error })))
        )
    )
  ));

  // Members

  deleteMember$ = createEffect(() => this.actions$.pipe(
    ofType(deleteMember),
    switchMap((action) =>
      this.memberService
        .delete(action.payload.id)
        .pipe(
          map((payload: Member) => memberDeleted({ payload })),
          catchError(error => of(memberDeleteError({ error })))
        )
    )
  ));

  constructor(
    private readonly actions$: Actions,
    private readonly store: Store<ProjectPartialState>,
    private readonly projectService: ProjectService,
    private readonly localeService: LocaleService,
    private readonly keyService: KeyService,
    private readonly memberService: MemberService,
    private readonly messageService: MessageService,
    private readonly activityService: ActivityService
  ) {
  }
}
