import { AccessToken, Project, User, UserFeatureFlag, UserRole } from '@dev/translatr-model';
import { map } from 'rxjs/operators';

// General

export const isAdmin = (user?: User): boolean => user !== undefined && user.role === UserRole.Admin;

// Users

export const hasCreateUserPermission = () => map(isAdmin);

export const hasUserPermissionToDeleteUser = (me: User, user: User) =>
  me !== undefined && me.id !== user.id && isAdmin(me);

export const hasEditUserPermission = (user: User) =>
  map((me?: User) => (me !== undefined && me.id === user.id) || isAdmin(me));

export const hasDeleteUserPermission = (user: User) =>
  map((me?: User) => hasUserPermissionToDeleteUser(me, user));

export const hasDeleteAllUsersPermission = (users: User[]) =>
  map((me?: User) =>
    users
      .map((user: User) => hasUserPermissionToDeleteUser(me, user))
      .reduce((acc: boolean, next: boolean) => acc && next, true)
  );

// Projects

export const hasUserPermissionToDeleteProject = (me: User, project: Project) =>
  (me !== undefined && me.id === project.ownerId) || isAdmin(me);

export const hasEditProjectPermission = (project: Project) =>
  map((me?: User) => (me !== undefined && me.id === project.ownerId) || isAdmin(me));

export const hasDeleteProjectPermission = (project: Project) =>
  map((me?: User) => hasUserPermissionToDeleteProject(me, project));

export const hasDeleteAllProjectsPermission = (projects: Project[]) =>
  map((me?: User) =>
    projects
      .map((project: Project) => hasUserPermissionToDeleteProject(me, project))
      .reduce((acc: boolean, next: boolean) => acc && next, true)
  );

// Access Tokens

export const hasUserPermissionToEditAccessToken = (me: User, accessToken: AccessToken) =>
  (me !== undefined && me.id === accessToken.userId) || isAdmin(me);

export const hasUserPermissionToDeleteAccessToken = (me: User, accessToken: AccessToken) =>
  (me !== undefined && me.id === accessToken.userId) || isAdmin(me);

export const hasEditAccessTokenPermission = (accessToken: AccessToken) =>
  map((me?: User) => hasUserPermissionToEditAccessToken(me, accessToken));

export const hasDeleteAccessTokenPermission = (accessToken: AccessToken) =>
  map((me?: User) => hasUserPermissionToDeleteAccessToken(me, accessToken));

export const hasDeleteAllAccessTokensPermission = (accessTokens: AccessToken[]) =>
  map((me?: User) =>
    accessTokens
      .map((accessToken: AccessToken) => hasUserPermissionToDeleteAccessToken(me, accessToken))
      .reduce((acc: boolean, next: boolean) => acc && next, true)
  );

// Feature Flags

export const hasUserPermissionToEditFeatureFlag = (me: User, featureFlag: UserFeatureFlag) =>
  (me !== undefined && me.id === featureFlag.userId) || isAdmin(me);

export const hasUserPermissionToDeleteFeatureFlag = (me: User, featureFlag: UserFeatureFlag) =>
  (me !== undefined && me.id === featureFlag.userId) || isAdmin(me);

export const hasEditFeatureFlagPermission = (featureFlag: UserFeatureFlag) =>
  map((me?: User) => hasUserPermissionToEditFeatureFlag(me, featureFlag));

export const hasDeleteFeatureFlagPermission = (featureFlag: UserFeatureFlag) =>
  map((me?: User) => hasUserPermissionToDeleteFeatureFlag(me, featureFlag));

export const hasDeleteAllFeatureFlagsPermission = (featureFlags: UserFeatureFlag[]) =>
  map((me?: User) =>
    featureFlags
      .map((featureFlag: UserFeatureFlag) => hasUserPermissionToDeleteFeatureFlag(me, featureFlag))
      .reduce((acc: boolean, next: boolean) => acc && next, true)
  );
