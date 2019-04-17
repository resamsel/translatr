import {AppAction, AppActionTypes} from './app.actions';
import {User} from "@dev/translatr-sdk";

export const APP_FEATURE_KEY = 'app';

/**
 * Interface for the 'App' data used in
 *  - AppState, and
 *  - appReducer
 *
 *  Note: replace if already defined in another module
 */

export interface AppState {
  me?: User;
}

export interface AppPartialState {
  readonly [APP_FEATURE_KEY]: AppState;
}

export const initialState: AppState = {};

export function appReducer(
  state: AppState = initialState,
  action: AppAction
): AppState {
  switch (action.type) {
    case AppActionTypes.MeLoaded: {
      return {
        ...state,
        me: action.payload,
      };
    }
    default:
      return state;
  }
}
