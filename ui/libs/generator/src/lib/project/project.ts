import {
  AccessToken,
  Key,
  Locale,
  Project,
  ProjectCriteria,
  Scope,
  User,
} from '@dev/translatr-model';
import {
  AccessTokenService,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService,
} from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import * as randomName from 'random-name';
import { combineLatest, Observable, of } from 'rxjs';
import { catchError, concatMap, filter, map, mapTo, retry } from 'rxjs/operators';
import * as _ from 'underscore';
import { chooseAccessToken } from '../access-token';
import { keyNames, localeNames } from '../constants';
import { selectRandomUserAccessToken } from '../user';
import { getRandomProject } from './get';

/**
 * Randomly choose user, create project for that user with random name.
 */
export const createRandomProject = (
  accessToken: AccessToken,
  user: User,
  projectService: ProjectService,
  localeService: LocaleService,
  keyService: KeyService,
  messageService: MessageService,
): Observable<{ project: Project; locales: Locale[]; keys: Key[] }> =>
  projectService
    .withAuth(accessToken.key)
    .create({
      name: randomName.place().replace(' ', ''),
      description: 'Generated',
      ownerId: user.id,
    })
    .pipe(
      retry(3),
      // A freshly created project has no contributors yet (only an owner, who isn't a
      // ProjectUser row) — set members: [] here so callers can treat the result the same
      // way as a project fetched with ?fetch=members, regardless of which path returned it.
      map((project) => ({ ...project, members: [] })),
      concatMap((project) =>
        combineLatest(
          _.sample(localeNames, Math.ceil(Math.random() * localeNames.length))
            .filter((name) => name !== undefined && name !== '')
            .map((localeName: string) =>
              localeService
                .withAuth(accessToken.key)
                .create({
                  name: localeName,
                  projectId: project.id,
                })
                .pipe(catchError(() => of(undefined))),
            ),
        ).pipe(map((locales: Locale[]) => ({ project, locales: locales.filter(Boolean) }))),
      ),
      concatMap(({ project, locales }) => {
        return combineLatest(
          _.sample(keyNames, Math.ceil((Math.random() * keyNames.length) / 10))
            .filter((name) => name !== undefined && name !== '')
            .map((keyName: string) =>
              keyService
                .withAuth(accessToken.key)
                .create({
                  name: keyName,
                  projectId: project.id,
                })
                .pipe(catchError(() => of(undefined))),
            ),
        ).pipe(map((keys: Key[]) => ({ project, locales, keys: keys.filter(Boolean) })));
      }),
      concatMap(({ project, locales, keys }) => {
        const locale = pickRandomly(locales);
        return combineLatest(
          keys.map((key: Key) =>
            messageService
              .withAuth(accessToken.key)
              .create({
                localeId: locale.id,
                keyId: key.id,
                value: `${key.name} (${locale.displayName})`,
              })
              .pipe(catchError(() => of(undefined))),
          ),
        ).pipe(mapTo({ project, locales, keys }));
      }),
    );

/**
 * Always returns a project - creates one if necessary
 */
export const selectRandomProject = (
  accessToken: AccessToken,
  user: User,
  projectService: ProjectService,
  localeService: LocaleService,
  keyService: KeyService,
  messageService: MessageService,
  criteria: ProjectCriteria = {},
): Observable<Project> => {
  return projectService
    .withAuth(accessToken.key)
    .find({
      order: 'whenUpdated desc',
      limit: 1,
      offset: Math.floor(Math.random() * 100),
      ...criteria,
    })
    .pipe(
      map((paged) => pickRandomly(paged.list)),
      concatMap((project) => {
        if (project === undefined) {
          return createRandomProject(
            accessToken,
            user,
            projectService,
            localeService,
            keyService,
            messageService,
          ).pipe(map((payload: { project: Project }) => payload.project));
        }
        return of(project);
      }),
    );
};

/**
 * Always returns a project - creates one if necessary
 */
export const selectRandomProjectAccessToken = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
  localeService: LocaleService,
  keyService: KeyService,
  messageService: MessageService,
  projectCriteria: ProjectCriteria = {},
): Observable<{ project: Project; accessToken: AccessToken }> => {
  return selectRandomUserAccessToken(accessTokenService, userService).pipe(
    concatMap(({ accessToken, user }) =>
      selectRandomProject(
        accessToken,
        user,
        projectService,
        localeService,
        keyService,
        messageService,
        projectCriteria,
      ).pipe(map((project) => ({ accessToken, project }))),
    ),
  );
};

/**
 * Randomly choose user, update project of that user randomly
 */
export const updateRandomProject = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
  localeService: LocaleService,
  keyService: KeyService,
  messageService: MessageService,
  defaultAccessToken: string,
): Observable<Project> => {
  return selectRandomProjectAccessToken(
    accessTokenService,
    userService,
    projectService,
    localeService,
    keyService,
    messageService,
    {
      limit: 10,
    },
  ).pipe(
    concatMap(({ accessToken, project }) =>
      projectService
        .withAuth(chooseAccessToken(accessToken, defaultAccessToken, Scope.ProjectWrite))
        .update({
          ...project,
          description: project.description.endsWith('!')
            ? project.description.replace('!', '')
            : `${project.description}!`,
        }),
    ),
  );
};

export const deleteRandomProject = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
): Observable<Project> => {
  // Randomly choose user, delete project of that user randomly
  return selectRandomUserAccessToken(accessTokenService, userService, {
    limit: 10,
  }).pipe(
    filter(
      (payload: { user: User; accessToken: AccessToken }) =>
        !!payload.user && !!payload.user.id && !!payload.accessToken && !!payload.accessToken.key,
    ),
    concatMap((payload: { user: User; accessToken: AccessToken }) =>
      getRandomProject(projectService, payload.user, payload.accessToken).pipe(
        map((project: Project) => ({ ...payload, project })),
      ),
    ),
    filter(
      (payload: { user: User; accessToken: AccessToken; project: Project }) =>
        payload.project !== undefined,
    ),
    concatMap((payload: { user: User; accessToken: AccessToken; project: Project }) =>
      projectService.withAuth(payload.accessToken.key).delete(payload.project.id),
    ),
  );
};
