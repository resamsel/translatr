import { Injector } from '@angular/core';
import { AccessToken, Locale, PagedList, Project, User, UserRole } from '@dev/translatr-model';
import { Observable, of } from 'rxjs';
import { errorMessage, LocaleService } from '@dev/translatr-sdk';
import { getRandomProject, getRandomUserAccessToken, State } from '.';
import { catchError, concatMap, map } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import { pickRandomly } from '@translatr/utils';
import { filter } from 'rxjs/internal/operators/filter';
import * as _ from 'underscore';

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

export const createLocale = (
  injector: Injector,
  locale: Locale,
  accessToken: AccessToken
): Observable<Locale> => {
  return injector
    .get(LocaleService)
    .create(locale, { params: { access_token: accessToken.key } });
};

export const deleteLocale = (
  injector: Injector,
  localeId: string,
  accessToken: AccessToken
): Observable<Locale> => {
  return injector
    .get(LocaleService)
    .delete(localeId, { params: { access_token: accessToken.key } });
};

export const createRandomLocale = (
  injector: Injector
): Observable<Partial<State>> => {
  return getRandomUserAccessToken(
    injector,
    {},
    (user: User) => user.role !== UserRole.Admin
  ).pipe(
    concatMap((payload: { user: User; accessToken: AccessToken }) =>
      getRandomProject(injector, payload.user, payload.accessToken).pipe(
        map((project: Project) => ({ ...payload, project }))
      )
    ),
    filter(
      (payload: { user: User; accessToken: AccessToken; project: Project }) =>
        payload.project !== undefined
    ),
    concatMap(
      (payload: { user: User; accessToken: AccessToken; project: Project }) =>
        injector
          .get(LocaleService)
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
    concatMap(
      (payload: {
        user: User;
        accessToken: AccessToken;
        project: Project;
        locales: Locale[];
      }) =>
        createLocale(
          injector,
          {
            name: pickRandomly(
              _.difference(
                localeNames,
                payload.locales.map((locale: Locale) => locale.name)
              )
            ),
            projectId: payload.project.id
          },
          payload.accessToken
        ).pipe(map((locale: Locale) => ({ ...payload, locale })))
    ),
    map(
      (payload: { user: User; project: Project; locale: Locale }) =>
        `${payload.user.name} created locale ${
          payload.locale.displayName
        } in project ${payload.project.name}`
    ),
    catchError((err: HttpErrorResponse) =>
      of(`Locale could not be created: ${errorMessage(err)}`)
    ),
    map((message: string) => ({ message }))
  );
};

export const deleteRandomLocale = (
  injector: Injector
): Observable<Partial<State>> => {
  return getRandomUserAccessToken(
    injector,
    {},
    (user: User) => user.role !== UserRole.Admin
  ).pipe(
    concatMap((payload: { user: User; accessToken: AccessToken }) =>
      getRandomProject(injector, payload.user, payload.accessToken).pipe(
        map((project: Project) => ({ ...payload, project }))
      )
    ),
    filter(
      (payload: { user: User; accessToken: AccessToken; project: Project }) =>
        payload.project !== undefined
    ),
    concatMap(
      (payload: { user: User; accessToken: AccessToken; project: Project }) =>
        injector
          .get(LocaleService)
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
    concatMap(
      (payload: {
        user: User;
        accessToken: AccessToken;
        project: Project;
        locales: Locale[];
      }) =>
        deleteLocale(
          injector,
          pickRandomly(payload.locales.map((locale: Locale) => locale.id)),
          payload.accessToken
        ).pipe(map((locale: Locale) => ({ ...payload, locale })))
    ),
    map(
      (payload: { user: User; project: Project; locale: Locale }) =>
        `${payload.user.name} deleted locale ${
          payload.locale.displayName
        } in project ${payload.project.name}`
    ),
    catchError((err: HttpErrorResponse) =>
      of(`Locale could not be deleted: ${errorMessage(err)}`)
    ),
    map((message: string) => ({ message }))
  );
};
