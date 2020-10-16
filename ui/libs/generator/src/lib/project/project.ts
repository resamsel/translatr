import { AccessToken, Key, Locale, PagedList, Project, User } from '@dev/translatr-model';
import {
  AccessTokenService,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import * as randomName from 'random-name';
import { combineLatest, Observable, of } from 'rxjs';
import { concatMap, filter, map, mapTo, retry } from 'rxjs/operators';
import * as _ from 'underscore';
import { keyNames } from '../key';
import { localeNames } from '../locale';
import { selectRandomUserAccessToken } from '../user';
import { getRandomProject } from './get';

/**
 * Randomly choose user, create project for that user with random name.
 */
export const createRandomProject = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
  localeService: LocaleService,
  keyService: KeyService,
  messageService: MessageService
): Observable<{ project: Project; locales: Locale[]; keys: Key[] }> => {
  return selectRandomUserAccessToken(accessTokenService, userService).pipe(
    filter(
      (payload: { user: User; accessToken: AccessToken }) =>
        !!payload.user && !!payload.user.id && !!payload.accessToken && !!payload.accessToken.key
    ),
    concatMap((payload: { user: User; accessToken: AccessToken }) =>
      projectService
        .create(
          {
            name: randomName.place().replace(' ', ''),
            description: 'Generated',
            ownerId: payload.user.id
          },
          {
            params: { access_token: payload.accessToken.key }
          }
        )
        .pipe(map((project: Project) => ({ ...payload, project })))
    ),
    retry(3),
    concatMap((payload: { user: User; project: Project; accessToken: AccessToken }) =>
      combineLatest(
        _.sample(localeNames, Math.ceil(Math.random() * localeNames.length))
          .filter(name => name !== undefined && name !== '')
          .map((localeName: string) =>
            localeService.create(
              {
                name: localeName,
                projectId: payload.project.id
              },
              { params: { access_token: payload.accessToken.key } }
            )
          )
      ).pipe(map((locales: Locale[]) => ({ ...payload, locales })))
    ),
    concatMap(
      (payload: { user: User; project: Project; locales: Locale[]; accessToken: AccessToken }) => {
        return combineLatest(
          _.sample(keyNames, Math.ceil((Math.random() * keyNames.length) / 10))
            .filter(name => name !== undefined && name !== '')
            .map((keyName: string) =>
              keyService.create(
                {
                  name: keyName,
                  projectId: payload.project.id
                },
                { params: { access_token: payload.accessToken.key } }
              )
            )
        ).pipe(map((keys: Key[]) => ({ ...payload, keys })));
      }
    ),
    concatMap(
      (payload: {
        user: User;
        project: Project;
        locales: Locale[];
        keys: Key[];
        accessToken: AccessToken;
      }) => {
        const locale = pickRandomly(payload.locales);
        return combineLatest(
          payload.keys.map((key: Key) =>
            messageService.create(
              {
                localeId: locale.id,
                keyId: key.id,
                value: `${key.name} (${locale.displayName})`
              },
              { params: { access_token: payload.accessToken.key } }
            )
          )
        ).pipe(mapTo(payload));
      }
    )
  );
};

export const selectRandomProject = (projectService: ProjectService): Observable<Project> => {
  return projectService
    .find({
      order: 'whenUpdated desc',
      limit: 1,
      offset: Math.floor(Math.random() * 100)
    })
    .pipe(map(paged => pickRandomly(paged.list)));
};

/**
 * Randomly choose user, update project of that user randomly
 */
export const updateRandomProject = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService
): Observable<Project> => {
  return selectRandomUserAccessToken(accessTokenService, userService, {
    limit: 10
  }).pipe(
    filter(
      (payload: { user: User; accessToken: AccessToken }) =>
        !!payload.user && !!payload.user.id && !!payload.accessToken && !!payload.accessToken.key
    ),
    concatMap((payload: { user: User; accessToken: AccessToken }) =>
      projectService.find({ owner: payload.user.id, access_token: payload.accessToken.key }).pipe(
        map((pagedList: PagedList<Project>) => ({
          user: payload.user,
          project: pickRandomly(pagedList.list),
          accessToken: payload.accessToken
        }))
      )
    ),
    concatMap((payload: { user: User; project: Project; accessToken: AccessToken }) => {
      if (!!payload.project) {
        return of(payload);
      }

      const project: Project = {
        name: randomName.place().replace(' ', ''),
        description: 'Generated'
      };
      return projectService
        .create(project, {
          params: { access_token: payload.accessToken.key }
        })
        .pipe(map((p: Project) => ({ ...payload, project: p })));
    }),
    concatMap(({ user, project, accessToken }) =>
      projectService.update(
        {
          ...project,
          description: project.description.endsWith('!')
            ? project.description.replace('!', '')
            : `${project.description}!`
        },
        { params: { access_token: accessToken.key } }
      )
    )
  );
};

export const deleteRandomProject = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService
): Observable<Project> => {
  // Randomly choose user, delete project of that user randomly
  return selectRandomUserAccessToken(accessTokenService, userService, {
    limit: 10
  }).pipe(
    filter(
      (payload: { user: User; accessToken: AccessToken }) =>
        !!payload.user && !!payload.user.id && !!payload.accessToken && !!payload.accessToken.key
    ),
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
      projectService.delete(payload.project.id, {
        params: { access_token: payload.accessToken.key }
      })
    )
  );
};
