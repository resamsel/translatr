import { Injector } from '@angular/core';
import { AccessToken, Key, PagedList, Project, User } from '@dev/translatr-model';
import { AccessTokenService, KeyService, ProjectService, UserService } from '@dev/translatr-sdk';
import { cartesianProduct, pickRandomly } from '@translatr/utils';
import { Observable } from 'rxjs';
import { concatMap, filter, map } from 'rxjs/operators';
import * as _ from 'underscore';
import { getRandomProject } from './project/get';
import { selectRandomUserAccessToken } from './user';

export const featureNames = [
  'user',
  'users',
  'project',
  'projects',
  'locale',
  'locales',
  'key',
  'keys',
  'message',
  'messages',
  'accessToken',
  'accessTokens',
  'member',
  'members',
  'activity',
  'activities',
  'dashboard',
  'admin'
];
export const parts = ['list', 'detail', 'find', 'search', 'main', 'header', 'footer'];
export const keySuffixes = [
  'title',
  'description',
  'text',
  'comment',
  'get',
  'create',
  'update',
  'delete',
  'permission',
  'sell',
  'confirm',
  'allow',
  'restricted',
  'filter',
  'clear'
];
export const keyNames = cartesianProduct([
  featureNames,
  parts,
  keySuffixes
]).map((values: string[]) => values.join('.'));

export const createKey = (
  injector: Injector,
  key: Key,
  accessToken: AccessToken
): Observable<Key> => {
  return injector.get(KeyService).create(key, { params: { access_token: accessToken.key } });
};

export const createRandomKey = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
  keyService: KeyService
): Observable<Key> => {
  return selectRandomUserAccessToken(accessTokenService, userService).pipe(
    concatMap((payload: { user: User; accessToken: AccessToken }) =>
      getRandomProject(projectService, payload.user, payload.accessToken).pipe(
        map((project: Project) => ({ ...payload, project }))
      )
    ),
    filter(({ project }) => project !== undefined),
    concatMap(({ user, accessToken, project }) =>
      keyService
        .find({
          projectId: project.id,
          access_token: accessToken.key
        })
        .pipe(
          map((paged: PagedList<Key>) => ({
            user,
            accessToken,
            project,
            keys: paged.list
          }))
        )
    ),
    map(({ user, accessToken, project, keys }) => ({
      user,
      accessToken,
      project,
      keys,
      keyName: pickRandomly(
        _.difference(
          keyNames,
          keys.map(key => key.name)
        )
      )
    })),
    filter(({ keyName }) => keyName !== undefined && keyName !== ''),
    concatMap(({ user, accessToken, project, keys, keyName }) =>
      keyService
        .create(
          {
            name: keyName,
            projectId: project.id
          },
          { params: { access_token: accessToken.key } }
        )
        .pipe(map(key => ({ user, accessToken, project, keys, keyName, key })))
    ),
    map(({ key }) => key)
  );
};

export const deleteRandomKey = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
  keyService: KeyService
): Observable<Key> => {
  return selectRandomUserAccessToken(accessTokenService, userService).pipe(
    concatMap((payload: { user: User; accessToken: AccessToken }) =>
      getRandomProject(projectService, payload.user, payload.accessToken).pipe(
        map((project: Project) => ({ ...payload, project }))
      )
    ),
    filter(
      (payload: { user: User; accessToken: AccessToken; project: Project }) =>
        payload.project !== undefined
    ),
    concatMap((payload: { user: User; accessToken: AccessToken; project: Project }) =>
      keyService
        .find({
          projectId: payload.project.id,
          access_token: payload.accessToken.key
        })
        .pipe(
          map((pagedList: PagedList<Key>) => ({
            ...payload,
            keys: pagedList.list
          }))
        )
    ),
    filter(
      (payload: { user: User; accessToken: AccessToken; project: Project; keys: Key[] }) =>
        payload.keys.length > 0
    ),
    concatMap((payload: { user: User; accessToken: AccessToken; project: Project; keys: Key[] }) =>
      keyService
        .delete(pickRandomly(payload.keys.map((key: Key) => key.id)), {
          params: { access_token: payload.accessToken.key }
        })
        .pipe(map((key: Key) => ({ ...payload, key })))
    ),
    map((payload: { user: User; project: Project; key: Key }) => payload.key)
  );
};
