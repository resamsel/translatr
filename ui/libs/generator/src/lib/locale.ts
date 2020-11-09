import { AccessToken, Locale, PagedList, Project, Scope } from '@dev/translatr-model';
import {
  AccessTokenService,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import { Observable, of } from 'rxjs';
import { concatMap, filter, map } from 'rxjs/operators';
import * as _ from 'underscore';
import { chooseAccessToken } from './access-token';
import { localeNames } from './constants';
import { selectRandomProjectAccessToken } from './project';

export const createRandomLocaleForProject = (
  localeService: LocaleService,
  project: Project,
  accessToken: AccessToken,
  defaultAccessToken: string
): Observable<Locale> =>
  localeService
    .find({
      projectId: project.id,
      access_token: chooseAccessToken(
        accessToken,
        defaultAccessToken,
        Scope.ProjectRead,
        Scope.LocaleRead
      )
    })
    .pipe(
      map((paged: PagedList<Locale>) => paged.list),
      map(locales => ({
        locales,
        localeName: pickRandomly(
          _.difference(
            localeNames,
            locales.map(locale => locale.name)
          )
        )
      })),
      filter(({ localeName }) => Boolean(localeName)),
      concatMap(({ locales, localeName }) =>
        localeService.create(
          {
            name: localeName,
            projectId: project.id
          },
          {
            params: {
              access_token: chooseAccessToken(
                accessToken,
                defaultAccessToken,
                Scope.ProjectRead,
                Scope.LocaleWrite
              )
            }
          }
        )
      )
    );

/**
 * Always returns a locale
 */
export const createRandomLocale = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
  localeService: LocaleService,
  keyService: KeyService,
  messageService: MessageService,
  defaultAccessToken: string
): Observable<Locale> => {
  return selectRandomProjectAccessToken(
    accessTokenService,
    userService,
    projectService,
    localeService,
    keyService,
    messageService
  ).pipe(
    concatMap(({ accessToken, project }) =>
      createRandomLocaleForProject(localeService, project, accessToken, defaultAccessToken)
    )
  );
};

export const selectRandomLocaleForProject = (
  localeService: LocaleService,
  project: Project,
  accessToken: AccessToken,
  defaultAccessToken: string
): Observable<Locale> =>
  localeService
    .find({
      projectId: project.id,
      access_token: chooseAccessToken(
        accessToken,
        defaultAccessToken,
        Scope.ProjectRead,
        Scope.LocaleRead
      ),
      limit: 200
    })
    .pipe(
      map(paged => pickRandomly(paged.list)),
      concatMap(locale => {
        if (Boolean(locale)) {
          return of(locale);
        }

        // Create a locale if none exists
        return createRandomLocaleForProject(
          localeService,
          project,
          accessToken,
          defaultAccessToken
        );
      })
    );

export const selectLocaleForProject = (
  localeName: string,
  localeService: LocaleService,
  project: Project,
  accessToken: AccessToken,
  defaultAccessToken: string
): Observable<Locale> =>
  localeService
    .find({
      projectId: project.id,
      access_token: chooseAccessToken(
        accessToken,
        defaultAccessToken,
        Scope.ProjectRead,
        Scope.LocaleRead
      ),
      limit: 200
    })
    .pipe(
      map(paged => paged.list.find(locale => locale.name === localeName)),
      concatMap(locale => {
        if (Boolean(locale)) {
          return of(locale);
        }

        // Create a locale if none exists
        return createRandomLocaleForProject(
          localeService,
          project,
          accessToken,
          defaultAccessToken
        );
      })
    );

export const deleteRandomLocale = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
  localeService: LocaleService,
  keyService: KeyService,
  messageService: MessageService,
  defaultAccessToken: string
): Observable<Locale> => {
  return selectRandomProjectAccessToken(
    accessTokenService,
    userService,
    projectService,
    localeService,
    keyService,
    messageService
  ).pipe(
    concatMap(({ accessToken, project }) =>
      localeService
        .find({
          projectId: project.id,
          access_token: chooseAccessToken(
            accessToken,
            defaultAccessToken,
            Scope.ProjectRead,
            Scope.LocaleRead
          )
        })
        .pipe(
          map((pagedList: PagedList<Locale>) => ({
            accessToken,
            project,
            locales: pagedList.list
          }))
        )
    ),
    filter(({ locales }) => locales.length > 0),
    concatMap(({ accessToken, project, locales }) =>
      localeService.delete(pickRandomly(locales.map(locale => locale.id)), {
        params: {
          access_token: chooseAccessToken(
            accessToken,
            defaultAccessToken,
            Scope.ProjectRead,
            Scope.LocaleWrite
          )
        }
      })
    )
  );
};
