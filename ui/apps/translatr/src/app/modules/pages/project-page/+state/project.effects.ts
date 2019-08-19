import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { ProjectPartialState } from './project.reducer';
import {
  KeysLoaded,
  LoadKeys,
  LoadLocales,
  LoadMessages,
  LoadProject,
  LoadProjectActivities,
  LoadProjectActivityAggregated,
  LocalesLoaded,
  MessagesLoaded,
  ProjectActionTypes,
  ProjectActivitiesLoaded,
  ProjectActivitiesLoadError,
  ProjectActivityAggregatedLoaded,
  ProjectActivityAggregatedLoadError,
  ProjectLoaded,
  ProjectLoadError,
  ProjectSaved,
  SaveProject
} from './project.actions';
import { ActivityService, KeyService, LocaleService, MessageService, ProjectService } from '@dev/translatr-sdk';
import { catchError, map, switchMap, withLatestFrom } from 'rxjs/operators';
import { Activity, Aggregate, Key, KeyCriteria, Locale, LocaleCriteria, Message, PagedList, Project } from '@dev/translatr-model';
import { of } from 'rxjs';
import { select, Store } from '@ngrx/store';
import { projectQuery } from './project.selectors';
import { MessageCriteria } from '@translatr/translatr-model/src/lib/model/message-criteria';

@Injectable()
export class ProjectEffects {
  @Effect() loadProject$ = this.actions$.pipe(
    ofType<LoadProject>(ProjectActionTypes.LoadProject),
    switchMap((action: LoadProject) => {
        const payload = action.payload;
        return this.projectService
          .getProjectByOwnerAndName(payload.username, payload.projectName)
          .pipe(
            map((p: Project) => new ProjectLoaded(p)),
            catchError(error => {
              console.error('Error', error);
              return of(new ProjectLoadError(error));
            })
          );
      }
    )
  );

  @Effect() loadLocales$ = this.actions$.pipe(
    ofType<LoadLocales>(ProjectActionTypes.LoadLocales),
    withLatestFrom(this.store.pipe(select(projectQuery.getLocalesSearch))),
    switchMap(([action, localesSearch]: [LoadLocales, LocaleCriteria]) =>
      this.localeService
        .find({
          ...localesSearch,
          ...action.payload
        })
        .pipe(
          map((payload: PagedList<Locale>) => new LocalesLoaded(payload))
        )
    )
  );

  @Effect() loadKeys$ = this.actions$.pipe(
    ofType<LoadKeys>(ProjectActionTypes.LoadKeys),
    withLatestFrom(this.store.pipe(select(projectQuery.getKeysSearch))),
    switchMap(([action, keysSearch]: [LoadKeys, KeyCriteria]) =>
      this.keyService
        .find({
          ...keysSearch,
          ...action.payload
        })
        .pipe(map((payload: PagedList<Key>) => new KeysLoaded(payload)))
    )
  );

  @Effect() loadMessages$ = this.actions$.pipe(
    ofType<LoadMessages>(ProjectActionTypes.LoadMessages),
    withLatestFrom(this.store.pipe(select(projectQuery.getMessagesSearch))),
    switchMap(([action, messagesSearch]: [LoadMessages, MessageCriteria]) =>
      this.messageService
        .find({
          ...messagesSearch,
          ...action.payload
        })
        .pipe(map((payload: PagedList<Message>) => new MessagesLoaded(payload)))
    )
  );

  @Effect() loadProjectActivity$ = this.actions$.pipe(
    ofType<LoadProjectActivityAggregated>(ProjectActionTypes.LoadProjectActivityAggregated),
    switchMap((action: LoadProjectActivityAggregated) => {
      return this.activityService
          .aggregated({ projectId: action.payload.id })
          .pipe(
            map(
              (p: PagedList<Aggregate>) =>
                new ProjectActivityAggregatedLoaded(p)
            ),
            catchError(error => of(new ProjectActivityAggregatedLoadError(error)))
          );
      }
    )
  );

  @Effect() loadProjectActivities$ = this.actions$.pipe(
    ofType<LoadProjectActivities>(ProjectActionTypes.LoadProjectActivities),
    switchMap((action: LoadProjectActivities) => {
      return this.activityService
          .find(action.payload)
          .pipe(
            map((p: PagedList<Activity>) => new ProjectActivitiesLoaded(p)),
            catchError(error => of(new ProjectActivitiesLoadError(error)))
          );
      }
    )
  );

  @Effect() saveProject$ = this.actions$.pipe(
    ofType<SaveProject>(ProjectActionTypes.SaveProject),
    switchMap((action: SaveProject) =>
      this.projectService
        .update(action.payload)
        .pipe(map((payload: Project) => new ProjectSaved(payload)))
    )
  );

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
