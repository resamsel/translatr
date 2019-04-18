import {AppAction, AppActionTypes} from './app.actions';
import {AccessToken, PagedList, Project, User} from "@dev/translatr-model";

export const APP_FEATURE_KEY = 'app';

export interface AppState {
  me?: User;
  users?: PagedList<User>;
  projects?: PagedList<Project>;
  accessTokens?: PagedList<AccessToken>;
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
    case AppActionTypes.LoggedInUserLoaded:
      return {
        ...state,
        me: action.payload
      };
    case AppActionTypes.UsersLoaded:
      return {
        ...state,
        users: action.payload
      };
    case AppActionTypes.ProjectsLoaded:
      return {
        ...state,
        projects: action.payload
      };
    case AppActionTypes.AccessTokensLoaded:
      return {
        ...state,
        accessTokens: action.payload
      };
    case AppActionTypes.UserCreated:
      return {
        ...state,
        users: {
          ...state.users,
          list: [...state.users.list, action.payload]
        }
      };
    case AppActionTypes.UserUpdated:
      return {
        ...state,
        users: {
          ...state.users,
          list: state.users.list.map((user: User) => user.id === action.payload.id ? action.payload : user)
        }
      };
    case AppActionTypes.UserDeleted:
      return {
        ...state,
        users: {
          ...state.users,
          list: state.users.list.filter((user: User) => user.id !== action.payload.id)
        }
      };
    case AppActionTypes.ProjectDeleted:
      return {
        ...state,
        projects: {
          ...state.projects,
          list: state.projects.list.filter((project: Project) => project.id !== action.payload.id)
        }
      };
  }
  return state;
}
