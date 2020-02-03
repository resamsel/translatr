import { createFeatureSelector, createSelector } from '@ngrx/store';
import { PROJECT_FEATURE_KEY, ProjectState } from './project.reducer';

// Lookup the 'Project' feature state managed by NgRx
const getProjectState = createFeatureSelector<ProjectState>(
  PROJECT_FEATURE_KEY
);

const getLoading = createSelector(
  getProjectState,
  (state: ProjectState) => state.loading
);
const getError = createSelector(
  getProjectState,
  (state: ProjectState) => state.error
);

const getProject = createSelector(
  getProjectState,
  (state: ProjectState) => state.project
);

const getLocale = createSelector(
  getProjectState,
  (state: ProjectState) => state.locale
);

const getLocaleError = createSelector(
  getProjectState,
  (state: ProjectState) => state.localeError
);

const getLocales = createSelector(
  getProjectState,
  (state: ProjectState) => state.locales
);

const getLocalesSearch = createSelector(
  getProjectState,
  (state: ProjectState) => state.localesSearch
);

const getKey = createSelector(
  getProjectState,
  (state: ProjectState) => state.key
);

const getKeyError = createSelector(
  getProjectState,
  (state: ProjectState) => state.keyError
);

const getKeys = createSelector(
  getProjectState,
  (state: ProjectState) => state.keys
);

const getKeysSearch = createSelector(
  getProjectState,
  (state: ProjectState) => state.keysSearch
);

const getMembersSearch = createSelector(
  getProjectState,
  (state: ProjectState) => state.membersSearch
);

const getMessages = createSelector(
  getProjectState,
  (state: ProjectState) => state.messages
);

const getMessagesSearch = createSelector(
  getProjectState,
  (state: ProjectState) => state.messagesSearch
);

const getMembers = createSelector(
  getProjectState,
  (state: ProjectState) => state.members
);

const getModifiers = createSelector(
  getProjectState,
  (state: ProjectState) => state.modifiers
);

const getMember = createSelector(
  getProjectState,
  (state: ProjectState) => state.member
);

const getActivityAggregated = createSelector(
  getProjectState,
  (state: ProjectState) => state.activityAggregated
);

const getActivities = createSelector(
  getProjectState,
  (state: ProjectState) => state.activities
);

export const projectQuery = {
  getLoading,
  getError,
  getProject,
  getLocale,
  getLocaleError,
  getLocales,
  getLocalesSearch,
  getKey,
  getKeyError,
  getKeys,
  getKeysSearch,
  getMessages,
  getMessagesSearch,
  getMember,
  getMembers,
  getModifiers,
  getMembersSearch,
  getActivityAggregated,
  getActivities
};
