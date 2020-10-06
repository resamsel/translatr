import { AccessToken, Locale, PagedList, Project, User } from '@dev/translatr-model';
import { AccessTokenService, LocaleService, ProjectService, UserService } from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import { Observable } from 'rxjs';
import { concatMap, filter, map } from 'rxjs/operators';
import * as _ from 'underscore';
import { getRandomProject } from './project/get';
import { selectRandomUserAccessToken } from './user';

export const localeNames = [
  'en',
  'de',
  'it',
  'fr',
  'hu',
  'sl',
  'cs',
  'es',
  'pl',
  'el',
  'el_GR',
  'en-US',
  'en-UK',
  'de-AT',
  'de-DE',
  'ar',
  'ro',
  'es',
  'sw',
  'th',
  'ja',
  'zh',
  'ca',
  'ko'
];

export const createRandomLocale = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
  localeService: LocaleService
): Observable<Locale> => {
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
      localeService
        .find({
          projectId: payload.project.id,
          access_token: payload.accessToken.key
        })
        .pipe(
          map((pagedList: PagedList<Locale>) => ({
            ...payload,
            locales: pagedList.list
          }))
        )
    ),
    map(
      (payload: { user: User; accessToken: AccessToken; project: Project; locales: Locale[] }) => ({
        ...payload,
        localeName: pickRandomly(
          _.difference(
            localeNames,
            payload.locales.map((locale: Locale) => locale.name)
          )
        )
      })
    ),
    filter(
      (payload: {
        user: User;
        accessToken: AccessToken;
        project: Project;
        locales: Locale[];
        localeName: string;
      }) => payload.localeName !== undefined && payload.localeName !== ''
    ),
    concatMap(
      (payload: {
        user: User;
        accessToken: AccessToken;
        project: Project;
        locales: Locale[];
        localeName: string;
      }) =>
        localeService
          .create(
            {
              name: payload.localeName,
              projectId: payload.project.id
            },
            { params: { access_token: payload.accessToken.key } }
          )
          .pipe(map((locale: Locale) => ({ ...payload, locale })))
    ),
    map((payload: { user: User; project: Project; locale: Locale }) => payload.locale)
  );
};

export const deleteRandomLocale = (
  accessTokenService: AccessTokenService,
  userService: UserService,
  projectService: ProjectService,
  localeService: LocaleService
): Observable<Locale> => {
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
      localeService
        .find({
          projectId: payload.project.id,
          access_token: payload.accessToken.key
        })
        .pipe(
          map((pagedList: PagedList<Locale>) => ({
            ...payload,
            locales: pagedList.list
          }))
        )
    ),
    filter(
      (payload: { user: User; accessToken: AccessToken; project: Project; locales: Locale[] }) =>
        payload.locales.length > 0
    ),
    concatMap(
      (payload: { user: User; accessToken: AccessToken; project: Project; locales: Locale[] }) =>
        localeService
          .delete(pickRandomly(payload.locales.map((locale: Locale) => locale.id)), {
            params: { access_token: payload.accessToken.key }
          })
          .pipe(map((locale: Locale) => ({ ...payload, locale })))
    ),
    map((payload: { user: User; project: Project; locale: Locale }) => payload.locale)
  );
};
