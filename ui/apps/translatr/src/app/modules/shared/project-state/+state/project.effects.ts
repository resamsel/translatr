import { Injectable } from '@angular/core';
import {
  AccessToken,
  Activity,
  Aggregate,
  Key,
  Locale,
  Member,
  Message,
  PagedList
} from '@dev/translatr-model';
import {
  AccessTokenService,
  ActivityService,
  KeyService,
  LocaleService,
  MemberService,
  MessageService
} from '@dev/translatr-sdk';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { select, Store } from '@ngrx/store';
import { of } from 'rxjs';
import { catchError, map, switchMap, withLatestFrom } from 'rxjs/operators';
import {
  accessTokensLoaded,
  accessTokensLoadError,
  createKey,
  createLocale,
  createMember,
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
  loadAccessTokens,
  loadKeys,
  loadLocales,
  loadMembers,
  loadMessages,
  loadProjectActivities,
  loadProjectActivityAggregated,
  localeCreated,
  localeCreateError,
  localeDeleted,
  localeDeleteError,
  localesLoaded,
  localeUpdated,
  localeUpdateError,
  memberCreated,
  memberCreateError,
  memberDeleted,
  memberDeleteError,
  membersLoaded,
  memberUpdated,
  memberUpdateError,
  messagesLoaded,
  projectActivitiesLoaded,
  projectActivitiesLoadError,
  projectActivityAggregatedLoaded,
  projectActivityAggregatedLoadError,
  updateKey,
  updateLocale,
  updateMember
} from './project.actions';
import { ProjectPartialState } from './project.reducer';
import { projectQuery } from './project.selectors';

@Injectable()
export class ProjectEffects {
  loadLocales$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadLocales),
      withLatestFrom(this.store.pipe(select(projectQuery.getLocalesSearch))),
      switchMap(([action, localesSearch]) =>
        this.localeService
          .find({
            ...localesSearch,
            ...action.payload
          })
          .pipe(map((payload: PagedList<Locale>) => localesLoaded({ payload })))
      )
    )
  );

  loadKeys$ = createEffect(() =>
    this.actions$.pipe(
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
    )
  );

  loadMembers$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadMembers),
      withLatestFrom(this.store.pipe(select(projectQuery.getMembersSearch))),
      switchMap(([action, membersSearch]) =>
        this.memberService
          .find({
            ...membersSearch,
            ...action.payload
          })
          .pipe(map((payload: PagedList<Member>) => membersLoaded({ payload })))
      )
    )
  );

  loadMessages$ = createEffect(() =>
    this.actions$.pipe(
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
    )
  );

  loadProjectActivity$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadProjectActivityAggregated),
      switchMap(action =>
        this.activityService.aggregated({ projectId: action.payload.id }).pipe(
          map((payload: PagedList<Aggregate>) => projectActivityAggregatedLoaded({ payload })),
          catchError(error => of(projectActivityAggregatedLoadError({ error })))
        )
      )
    )
  );

  loadProjectActivities$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadProjectActivities),
      switchMap(action =>
        this.activityService
          .find({
            limit: 20,
            ...action.payload
          })
          .pipe(
            map((payload: PagedList<Activity>) => projectActivitiesLoaded({ payload })),
            catchError(error => of(projectActivitiesLoadError({ error })))
          )
      )
    )
  );

  // Locales

  createLocale$ = createEffect(() =>
    this.actions$.pipe(
      ofType(createLocale),
      switchMap(action =>
        this.localeService.create(action.payload).pipe(
          map((payload: Locale) => localeCreated({ payload })),
          catchError(error => of(localeCreateError({ error })))
        )
      )
    )
  );

  updateLocale$ = createEffect(() =>
    this.actions$.pipe(
      ofType(updateLocale),
      switchMap(action =>
        this.localeService.update(action.payload).pipe(
          map((payload: Locale) => localeUpdated({ payload })),
          catchError(error => of(localeUpdateError({ error })))
        )
      )
    )
  );

  deleteLocale$ = createEffect(() =>
    this.actions$.pipe(
      ofType(deleteLocale),
      switchMap(action =>
        this.localeService.delete(action.payload.id).pipe(
          map((payload: Locale) => localeDeleted({ payload })),
          catchError(error => of(localeDeleteError({ error })))
        )
      )
    )
  );

  // Keys

  createKey$ = createEffect(() =>
    this.actions$.pipe(
      ofType(createKey),
      switchMap(action =>
        this.keyService.create(action.payload).pipe(
          map((payload: Key) => keyCreated({ payload })),
          catchError(error => of(keyCreateError({ error })))
        )
      )
    )
  );

  updateKey$ = createEffect(() =>
    this.actions$.pipe(
      ofType(updateKey),
      switchMap(action =>
        this.keyService.update(action.payload).pipe(
          map((payload: Key) => keyUpdated({ payload })),
          catchError(error => of(keyUpdateError({ error })))
        )
      )
    )
  );

  deleteKey$ = createEffect(() =>
    this.actions$.pipe(
      ofType(deleteKey),
      switchMap(action =>
        this.keyService.delete(action.payload.id).pipe(
          map((payload: Key) => keyDeleted({ payload })),
          catchError(error => of(keyDeleteError({ error })))
        )
      )
    )
  );

  // Members

  createMember$ = createEffect(() =>
    this.actions$.pipe(
      ofType(createMember),
      switchMap(action =>
        this.memberService.create(action.payload).pipe(
          map((payload: Member) => memberCreated({ payload })),
          catchError(error => of(memberCreateError({ error })))
        )
      )
    )
  );

  updateMember$ = createEffect(() =>
    this.actions$.pipe(
      ofType(updateMember),
      switchMap(action =>
        this.memberService.update(action.payload).pipe(
          map((payload: Member) => memberUpdated({ payload })),
          catchError(error => of(memberUpdateError({ error })))
        )
      )
    )
  );

  deleteMember$ = createEffect(() =>
    this.actions$.pipe(
      ofType(deleteMember),
      switchMap(action =>
        this.memberService.delete(action.payload.id).pipe(
          map((payload: Member) => memberDeleted({ payload })),
          catchError(error => of(memberDeleteError({ error })))
        )
      )
    )
  );

  // Access Tokens

  loadAccessTokens$ = createEffect(() =>
    this.actions$.pipe(
      ofType(loadAccessTokens),
      switchMap(action =>
        this.accessTokenService.find(action.payload).pipe(
          map((payload: PagedList<AccessToken>) => accessTokensLoaded({ payload })),
          catchError(error => of(accessTokensLoadError({ error })))
        )
      )
    )
  );

  constructor(
    private readonly actions$: Actions,
    private readonly store: Store<ProjectPartialState>,
    private readonly localeService: LocaleService,
    private readonly keyService: KeyService,
    private readonly memberService: MemberService,
    private readonly messageService: MessageService,
    private readonly activityService: ActivityService,
    private readonly accessTokenService: AccessTokenService
  ) {}
}
