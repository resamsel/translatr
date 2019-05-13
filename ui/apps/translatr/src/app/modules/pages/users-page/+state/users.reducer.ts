import {UsersAction, UsersActionTypes} from './users.actions';
import {PagedList, User} from '@dev/translatr-model';

export const USERS_FEATURE_KEY = 'users';

/**
 * Interface for the 'Users' data used in
 *  - UsersState, and
 *  - usersReducer
 *
 *  Note: replace if already defined in another module
 */

export interface UsersState {
  list?: PagedList<User>; // list of Users; analogous to a sql normalized table
}

export interface UsersPartialState {
  readonly [USERS_FEATURE_KEY]: UsersState;
}

export const initialState: UsersState = {};

export function usersReducer(
  state: UsersState = initialState,
  action: UsersAction
): UsersState {
  switch (action.type) {
    case UsersActionTypes.UsersLoaded: {
      return {
        ...state,
        list: action.payload
      };
    }
    default:
      return state;
  }
}
