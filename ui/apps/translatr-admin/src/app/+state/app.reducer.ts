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
    case AppActionTypes.UserLoaded:
      return {
        ...state,
        users: state.users
          ? {...state.users, list: [...state.users.list, action.payload]}
          : {list: [action.payload], hasNext: false, hasPrev: false, offset: 0, limit: 1}
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
    case AppActionTypes.UsersDeleted:
      return {
        ...state,
        users: {
          ...state.users,
          list: state.users.list.filter((user: User) =>
            action.payload.find((deleted: User) => user.id !== deleted.id))
        }
      };

    // Projects

    case AppActionTypes.ProjectsLoaded:
      return {
        ...state,
        projects: action.payload
      };
    case AppActionTypes.ProjectDeleted:
      return {
        ...state,
        projects: {
          ...state.projects,
          list: state.projects.list.filter((project: Project) => project.id !== action.payload.id)
        }
      };
    case AppActionTypes.ProjectsDeleted:
      return {
        ...state,
        projects: {
          ...state.projects,
          list: state.projects.list.filter((project: Project) =>
            action.payload.find((deleted: Project) => project.id !== deleted.id))
        }
      };

    // Access Tokens

    case AppActionTypes.AccessTokensLoaded:
      return {
        ...state,
        accessTokens: action.payload
      };
    case AppActionTypes.AccessTokenDeleted:
      return {
        ...state,
        accessTokens: {
          ...state.accessTokens,
          list: state.accessTokens.list.filter((accessToken: AccessToken) => accessToken.id !== action.payload.id)
        }
      };
    case AppActionTypes.AccessTokensDeleted:
      return {
        ...state,
        accessTokens: {
          ...state.accessTokens,
          list: state.accessTokens.list.filter((accessToken: AccessToken) =>
            action.payload.find((deleted: AccessToken) => accessToken.id !== deleted.id))
        }
      };

    default:
      return state;
  }
}
