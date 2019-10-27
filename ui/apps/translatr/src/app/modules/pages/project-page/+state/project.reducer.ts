import {
  createKey,
  createLocale,
  deleteKey,
  deleteLocale,
  keyCreated,
  keyDeleted,
  keysLoaded,
  keyUpdated,
  loadKeys,
  loadLocales,
  loadProject,
  localeCreated,
  localeDeleted,
  localesLoaded,
  localeUpdated,
  messagesLoaded,
  projectActivitiesLoaded,
  projectActivityAggregatedLoaded,
  projectLoaded,
  projectSaved,
  unloadProject,
  updateKey,
  updateLocale
} from './project.actions';
import { Activity, Aggregate, Key, Locale, Message, PagedList, Project, RequestCriteria } from '@dev/translatr-model';
import { Action, createReducer, on } from '@ngrx/store';
import { Identifiable } from '../../../shared/edit-form/abstract-edit-form-component';

export const PROJECT_FEATURE_KEY = 'project';

export interface ProjectState {
  project?: Project;

  locales?: PagedList<Locale>;
  localesSearch: RequestCriteria;
  locale?: Locale;
  localeError?: any;

  keys?: PagedList<Key>;
  keysSearch: RequestCriteria;
  key?: Key;
  keyError?: any;

  messages?: PagedList<Message>;
  messagesSearch: RequestCriteria;
  activityAggregated?: PagedList<Aggregate>;
  activities?: PagedList<Activity>;
  loading: boolean;
  error?: any; // last none error (if any)
}

export interface ProjectPartialState {
  readonly [PROJECT_FEATURE_KEY]: ProjectState;
}

export const initialState: ProjectState = {
  localesSearch: {
    limit: 50,
    offset: 0,
    order: 'name asc',
    fetch: 'count'
  },
  keysSearch: {
    limit: 50,
    offset: 0,
    order: 'name asc',
    fetch: 'count'
  },
  messagesSearch: {
    limit: 50,
    offset: 0,
    order: 'k.when_updated asc',
    fetch: 'count'
  },
  loading: false
};

const pagedListInsert = <T extends Identifiable>(
  pagedList: PagedList<T>, payload: T): PagedList<T> => ({
  ...pagedList,
  list: [...pagedList.list, payload]
});

const pagedListUpdate = <T extends Identifiable>(
  pagedList: PagedList<T>, payload: T): PagedList<T> => ({
  ...pagedList,
  list: pagedList.list.map(i => i.id === payload.id ? payload : i)
});

const pagedListDelete = <T extends Identifiable>(
  pagedList: PagedList<T>, payload: T): PagedList<T> => ({
  ...pagedList,
  list: pagedList.list.filter(i => i.id !== payload.id)
});

const resetLocale = (state: ProjectState): ProjectState => ({
  ...state,
  locale: undefined,
  localeError: undefined
});

const resetKey = (state: ProjectState): ProjectState => ({
  ...state,
  key: undefined,
  keyError: undefined
});

const reducer = createReducer(
  initialState,
  on(
    loadProject,
    (state, action) => ({ ...state, loading: true })
  ),
  on(
    projectLoaded,
    (state, { payload }) => ({ ...state, project: payload, loading: false })
  ),
  on(
    loadLocales,
    (state, { payload }) => ({
      ...state,
      localesSearch: {
        ...state.localesSearch,
        ...(payload ? payload : {})
      }
    })
  ),
  on(
    localesLoaded,
    (state, { payload }) => ({ ...state, locales: payload })
  ),
  on(
    createLocale,
    (state, { payload }) => resetLocale(state)
  ),
  on(
    localeCreated,
    (state, { payload }) => ({
      ...state,
      locale: payload,
      locales: pagedListInsert(state.locales, payload)
    })
  ),
  on(
    updateLocale,
    (state, { payload }) => resetLocale(state)
  ),
  on(
    localeUpdated,
    (state, { payload }) => ({
      ...state,
      locale: payload,
      locales: pagedListUpdate(state.locales, payload)
    })
  ),
  on(
    deleteLocale,
    (state, { payload }) => resetLocale(state)
  ),
  on(
    localeDeleted,
    (state, { payload }) => ({
      ...state,
      locale: payload,
      locales: pagedListDelete(state.locales, payload)
    })
  ),
  on(
    loadKeys,
    (state, { payload }) => ({
      ...state,
      keysSearch: {
        ...state.keysSearch,
        ...(payload ? payload : {})
      }
    })
  ),
  on(
    keysLoaded,
    (state, { payload }) => ({ ...state, keys: payload })
  ),
  on(
    createKey,
    (state, { payload }) => resetKey(state)
  ),
  on(
    keyCreated,
    (state, { payload }) => ({
      ...state,
      key: payload,
      keys: pagedListInsert(state.keys, payload)
    })
  ),
  on(
    updateKey,
    (state, { payload }) => resetKey(state)
  ),
  on(
    keyUpdated,
    (state, { payload }) => ({
      ...state,
      key: payload,
      keys: pagedListUpdate(state.keys, payload)
    })
  ),
  on(
    deleteKey,
    (state, { payload }) => resetKey(state)
  ),
  on(
    keyDeleted,
    (state, { payload }) => ({
      ...state,
      key: payload,
      keys: pagedListDelete(state.keys, payload)
    })
  ),
  on(
    messagesLoaded,
    (state, { payload }) => ({ ...state, messages: payload })
  ),
  on(
    projectActivityAggregatedLoaded,
    (state, { payload }) => ({ ...state, activityAggregated: payload })
  ),
  on(
    projectActivitiesLoaded,
    (state, { payload }) => ({ ...state, activities: payload })
  ),
  on(
    projectSaved,
    (state, { payload }) => ({ ...state, project: payload, loading: false })
  ),
  on(
    unloadProject,
    (state) => ({ ...initialState })
  )
);

export function projectReducer(state: ProjectState | undefined, action: Action):
  ProjectState {
  return reducer(state, action);
}
