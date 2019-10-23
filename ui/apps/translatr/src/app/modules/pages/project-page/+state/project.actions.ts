import { createAction, props } from '@ngrx/store';
import {
  Activity,
  ActivityCriteria,
  Aggregate,
  Key,
  KeyCriteria,
  Locale,
  LocaleCriteria,
  Message,
  MessageCriteria,
  PagedList,
  Project
} from '@dev/translatr-model';

export const loadProject = createAction(
  '[Project Page] LoadProject',
  props<{ payload: { username: string; projectName: string } }>()
);

export const projectLoadError = createAction(
  '[Projects API] Project Load Error',
  props<{ error: any }>()
);

export const projectLoaded = createAction(
  '[Projects API] Project Loaded',
  props<{ payload: Project }>()
);

export const loadLocales = createAction(
  '[Project Page] Load Locales',
  props<{ payload: LocaleCriteria }>()
);

export const localesLoadError = createAction(
  '[Locales API] Locales Load Error',
  props<{ error: any }>()
);

export const localesLoaded = createAction(
  '[Locales API] Locales Loaded',
  props<{ payload: PagedList<Locale> }>()
);

export const deleteLocale = createAction(
  '[Project Page] Delete Locale',
  props<{ payload: { id: string } }>()
);

export const localeDeleted = createAction(
  '[Locales API] Locale Deleted',
  props<{ payload: Locale }>()
);

export const localeDeleteError = createAction(
  '[Locales API] Locale Delete Error',
  props<{ error: any }>()
);

export const loadKeys = createAction(
  '[Project Page] Load Keys',
  props<{ payload: KeyCriteria }>()
);

export const keysLoadError = createAction(
  '[Keys API] Keys Load Error',
  props<{ error: any }>()
);

export const keysLoaded = createAction(
  '[Keys API] Keys Loaded',
  props<{ payload: PagedList<Key> }>()
);

export const deleteKey = createAction(
  '[Project Page] Delete Key',
  props<{ payload: { id: string } }>()
);

export const keyDeleted = createAction(
  '[Keys API] Key Deleted',
  props<{ payload: Key }>()
);

export const keyDeleteError = createAction(
  '[Keys API] Key Delete Error',
  props<{ error: any }>()
);

export const loadMessages = createAction(
  '[Project Page] Load Messages',
  props<{ payload: MessageCriteria }>()
);

export const messagesLoadError = createAction(
  '[Messages API] Messages Load Error',
  props<{ error: any }>()
);

export const messagesLoaded = createAction(
  '[Messages API] Messages Loaded',
  props<{ payload: PagedList<Message> }>()
);

export const loadProjectActivityAggregated = createAction(
  '[Project Page] Load Project Activity Aggregated',
  props<{ payload: { id: string } }>()
);

export const projectActivityAggregatedLoadError = createAction(
  '[Projects API] Project Activity Aggregated Load Error',
  props<{ error: any }>()
);

export const projectActivityAggregatedLoaded = createAction(
  '[Projects API] Project Activity Aggregated Loaded',
  props<{ payload: PagedList<Aggregate> }>()
);

export const loadProjectActivities = createAction(
  '[Project Page] Load Project Activities',
  props<{ payload: ActivityCriteria }>()
);

export const projectActivitiesLoadError = createAction(
  '[Projects API] Project Activities Load Error',
  props<{ error: any }>()
);

export const projectActivitiesLoaded = createAction(
  '[Projects API] Project Activities Loaded',
  props<{ payload: PagedList<Activity> }>()
);

export const saveProject = createAction(
  '[Project Page] Save Project',
  props<{ payload: Project }>()
);

export const projectSaved = createAction(
  '[Projects API] Project Saved',
  props<{ payload: Project }>()
);

export const unloadProject = createAction(
  '[Projects Page] Unload Project'
);
