import {
  deleteKey,
  deleteLocale,
  keyDeleted,
  keysLoaded,
  loadKeys,
  loadLocales,
  loadProject,
  localeDeleted,
  localesLoaded,
  messagesLoaded,
  projectActivitiesLoaded,
  projectActivityAggregatedLoaded,
  projectLoaded,
  projectSaved,
  unloadProject
} from './project.actions';
import { Activity, Aggregate, Key, Locale, Message, PagedList, Project, RequestCriteria } from '@dev/translatr-model';
import { Action, createReducer, on } from '@ngrx/store';

export const PROJECT_FEATURE_KEY = 'project';

export interface ProjectState {
  project?: Project;

  locales?: PagedList<Locale>;
  localesSearch: RequestCriteria;
  localeDeleted?: Locale;
  localeDeleteError?: any;

  keys?: PagedList<Key>;
  keysSearch: RequestCriteria;
  keyDeleted?: Key;
  keyDeleteError?: any;

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
    deleteLocale,
    (state, { payload }) => ({ ...state, localeDeleted: undefined })
  ),
  on(
    localeDeleted,
    (state, { payload }) => ({
      ...state,
      localeDeleted: payload,
      locales: {
        ...state.locales,
        list: state.locales.list.filter(i => i.id !== payload.id)
      }
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
    deleteKey,
    (state, { payload }) => ({ ...state, keyDeleted: undefined })
  ),
  on(
    keyDeleted,
    (state, { payload }) => ({
      ...state,
      keyDeleted: payload,
      keys: {
        ...state.keys,
        list: state.keys.list.filter(i => i.id !== payload.id)
      }
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
