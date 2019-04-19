import {AccessToken, Project, User, UserRole} from "@dev/translatr-model";
import {map} from "rxjs/operators";

// General

export const isAdmin = (user?: User): boolean => user !== undefined && user.role === UserRole.Admin;

// Users

export const hasCreateUserPermission = () => map(isAdmin);

export const hasEditUserPermission = (user: User) => map((me?: User) =>
  (me !== undefined && me.id === user.id) || isAdmin(me));

export const hasDeleteUserPermission = (user: User) => map((me?: User) =>
  (me !== undefined && me.id !== user.id) && isAdmin(me));

export const hasDeleteAllUsersPermission = (users: User[]) => map((me?: User) =>
  users.map((user: User) =>
    (me !== undefined && me.id !== user.id) && isAdmin(me)).reduce((acc: boolean, next: boolean) => acc && next, true));

// Projects

export const hasEditProjectPermission = (project: Project) => map((me?: User) =>
  (me !== undefined && me.id === project.ownerId) || isAdmin(me));

export const hasDeleteProjectPermission = (project: Project) => map((me?: User) =>
  (me !== undefined && me.id === project.ownerId) || isAdmin(me));

// Access Tokens

export const hasDeleteAccessTokenPermission = (accessToken: AccessToken) => map((me?: User) =>
  (me !== undefined && me.id !== accessToken.userId) || isAdmin(me));

