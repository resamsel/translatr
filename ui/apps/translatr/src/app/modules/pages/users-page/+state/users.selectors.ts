import { createFeatureSelector, createSelector } from '@ngrx/store';
import { USERS_FEATURE_KEY, UsersState } from './users.reducer';

// Lookup the 'Users' feature state managed by NgRx
const getUsersState = createFeatureSelector<UsersState>(USERS_FEATURE_KEY);

const getUsers = createSelector(getUsersState, (state: UsersState) => state.list);

export const usersQuery = {
  getUsers
};
