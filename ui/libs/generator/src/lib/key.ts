import { Injector } from '@angular/core';
import { AccessToken, Key, PagedList, Project, Scope } from '@dev/translatr-model';
import { AccessTokenService, KeyService, ProjectService } from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import { Observable, of } from 'rxjs';
import { concatMap, filter, map } from 'rxjs/operators';
import * as _ from 'underscore';
import { chooseAccessToken } from './access-token';
import { keyNames } from './constants';
import { selectRandomProjectAccessToken } from './project';

export const createKey = (
  injector: Injector,
  key: Key,
  accessToken: AccessToken
): Observable<Key> => {
  return injector.get(KeyService).create(key, { params: { access_token: accessToken.key } });
};

export const createRandomKeyForProject = (
  keyService: KeyService,
  project: Project,
  accessToken: AccessToken,
  defaultAccessToken: string
): Observable<Key> =>
  keyService
    .find({
      projectId: project.id,
      access_token: chooseAccessToken(
        accessToken,
        defaultAccessToken,
        Scope.ProjectRead,
        Scope.KeyRead
      )
    })
    .pipe(
      map((paged: PagedList<Key>) => paged.list),
      map(keys => ({
        keys,
        keyName: pickRandomly(
          _.difference(
            keyNames,
            keys.map(key => key.name)
          )
        )
      })),
      filter(({ keyName }) => keyName !== undefined && keyName !== ''),
      concatMap(({ keys, keyName }) =>
        keyService.create(
          {
            name: keyName,
            projectId: project.id
          },
          {
            params: {
              access_token: chooseAccessToken(
                accessToken,
                defaultAccessToken,
                Scope.ProjectRead,
                Scope.KeyWrite
              )
            }
          }
        )
      )
    );

export const createRandomKey = (
  accessTokenService: AccessTokenService,
  projectService: ProjectService,
  keyService: KeyService,
  defaultAccessToken: string
): Observable<Key> => {
  return selectRandomProjectAccessToken(accessTokenService, projectService).pipe(
    filter(({ project }) => project !== undefined),
    concatMap(({ project, accessToken }) =>
      createRandomKeyForProject(keyService, project, accessToken, defaultAccessToken)
    )
  );
};

export const selectRandomKeyForProject = (
  keyService: KeyService,
  project: Project,
  accessToken: AccessToken,
  defaultAccessToken: string
): Observable<Key> =>
  keyService
    .find({
      projectId: project.id,
      access_token: chooseAccessToken(
        accessToken,
        defaultAccessToken,
        Scope.ProjectRead,
        Scope.KeyRead
      ),
      limit: 200
    })
    .pipe(
      map(paged => pickRandomly(paged.list)),
      concatMap(key => {
        if (Boolean(key)) {
          return of(key);
        }

        // Create a key if none exists
        return createRandomKeyForProject(keyService, project, accessToken, defaultAccessToken);
      })
    );

export const deleteRandomKey = (
  accessTokenService: AccessTokenService,
  projectService: ProjectService,
  keyService: KeyService,
  defaultAccessToken: string
): Observable<Key> => {
  return selectRandomProjectAccessToken(accessTokenService, projectService).pipe(
    filter(({ project }) => Boolean(project)),
    concatMap(({ accessToken, project }) =>
      keyService
        .find({
          projectId: project.id,
          access_token: chooseAccessToken(
            accessToken,
            defaultAccessToken,
            Scope.ProjectRead,
            Scope.KeyRead
          )
        })
        .pipe(
          map((pagedList: PagedList<Key>) => ({
            accessToken,
            project,
            keys: pagedList.list
          }))
        )
    ),
    filter(({ keys }) => keys.length > 0),
    concatMap(({ accessToken, project, keys }) =>
      keyService.delete(pickRandomly(keys.map((key: Key) => key.id)), {
        params: {
          access_token: chooseAccessToken(
            accessToken,
            defaultAccessToken,
            Scope.ProjectRead,
            Scope.KeyWrite
          )
        }
      })
    )
  );
};
