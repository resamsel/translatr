import { AccessToken, Activity, Aggregate, Key, Locale, Member, Message, PagedList, RequestCriteria } from '@dev/translatr-model';
import { Action, createReducer, on } from '@ngrx/store';
import { Identifiable } from '../../../shared/edit-form/abstract-edit-form-component';
import {
  accessTokensLoaded,
  createKey,
  createLocale,
  createMember,
  deleteKey,
  deleteLocale,
  deleteMember,
  keyCreated,
  keyDeleted,
  keysLoaded,
  keyUpdated,
  localeCreated,
  localeDeleted,
  localesLoaded,
  localeUpdated,
  memberCreated,
  memberCreateError,
  memberDeleted,
  membersLoaded,
  memberUpdated,
  messagesLoaded,
  projectActivitiesLoaded,
  projectActivityAggregatedLoaded,
  unloadProject,
  updateKey,
  updateLocale,
  updateMember
} from './project.actions';

export const PROJECT_FEATURE_KEY = 'project';

export interface ProjectState {
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

  members?: PagedList<Member>;
  membersSearch: RequestCriteria;
  member?: Member;
  memberError?: any;

  activityAggregated?: PagedList<Aggregate>;
  activities?: PagedList<Activity>;

  accessTokens?: PagedList<AccessToken>;

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
    fetch: 'count,progress'
  },
  keysSearch: {
    limit: 50,
    offset: 0,
    order: 'name asc',
    fetch: 'count,progress'
  },
  membersSearch: {
    limit: 1000,
    offset: 0
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
  pagedList: PagedList<T>,
  payload: T
): PagedList<T> => ({
  ...pagedList,
  list: [...pagedList.list, payload]
});

const pagedListUpdate = <T extends Identifiable>(
  pagedList: PagedList<T>,
  payload: T
): PagedList<T> => ({
  ...pagedList,
  list: pagedList.list.map((i) => (i.id === payload.id ? payload : i))
});

const pagedListDelete = <T extends Identifiable>(
  pagedList: PagedList<T>,
  payload: T
): PagedList<T> => ({
  ...pagedList,
  list: pagedList.list.filter((i) => i.id !== payload.id)
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

const resetMember = (state: ProjectState): ProjectState => ({
  ...state,
  member: undefined,
  memberError: undefined
});

const reducer = createReducer(
  initialState,
  on(localesLoaded, (state, { payload }) => ({ ...state, locales: payload })),
  on(createLocale, (state) => resetLocale(state)),
  on(localeCreated, (state, { payload }) => ({
    ...state,
    locale: payload,
    locales: pagedListInsert(state.locales, payload)
  })),
  on(updateLocale, (state) => resetLocale(state)),
  on(localeUpdated, (state, { payload }) => ({
    ...state,
    locale: payload,
    locales: pagedListUpdate(state.locales, payload)
  })),
  on(deleteLocale, (state) => resetLocale(state)),
  on(localeDeleted, (state, { payload }) => ({
    ...state,
    locale: payload,
    locales: pagedListDelete(state.locales, payload)
  })),
  on(keysLoaded, (state, { payload }) => ({ ...state, keys: payload })),
  on(createKey, (state) => resetKey(state)),
  on(keyCreated, (state, { payload }) => ({
    ...state,
    key: payload,
    keys: pagedListInsert(state.keys, payload)
  })),
  on(updateKey, (state) => resetKey(state)),
  on(keyUpdated, (state, { payload }) => ({
    ...state,
    key: payload,
    keys: pagedListUpdate(state.keys, payload)
  })),
  on(deleteKey, (state) => resetKey(state)),
  on(keyDeleted, (state, { payload }) => ({
    ...state,
    key: payload,
    keys: pagedListDelete(state.keys, payload)
  })),
  on(membersLoaded, (state, { payload }) => ({ ...state, members: payload })),
  on(createMember, (state) => resetMember(state)),
  on(memberCreated, (state, { payload }) => ({
    ...state,
    member: payload,
    members: pagedListInsert(state.members, payload)
  })),
  on(memberCreateError, (state, error) => ({
    ...state,
    memberError: error
  })),
  on(updateMember, (state) => resetMember(state)),
  on(memberUpdated, (state, { payload }) => ({
    ...state,
    member: payload,
    members: pagedListUpdate(state.members, payload)
  })),
  on(deleteMember, (state) => resetMember(state)),
  on(memberDeleted, (state, { payload }) => ({
    ...state,
    member: payload,
    members: pagedListDelete(state.members, payload)
  })),
  on(messagesLoaded, (state, { payload }) => ({ ...state, messages: payload })),
  on(projectActivityAggregatedLoaded, (state, { payload }) => ({
    ...state,
    activityAggregated: payload
  })),
  on(projectActivitiesLoaded, (state, { payload }) => ({ ...state, activities: payload })),
  on(accessTokensLoaded, (state, { payload }) => ({ ...state, accessTokens: payload })),
  on(unloadProject, () => initialState)
);

export function projectReducer(state: ProjectState | undefined, action: Action): ProjectState {
  return reducer(state, action);
}
