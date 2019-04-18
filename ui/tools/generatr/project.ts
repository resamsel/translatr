import {Injector} from "@angular/core";
import {Observable, of} from "rxjs";
import {State} from "./state";
import {getRandomUser, getRandomUserAccessToken} from "./user";
import {AccessToken, Key, Locale, Message, PagedList, Project, User, UserRole} from "@dev/translatr-model";
import {catchError, concatMap, map, mapTo} from "rxjs/operators";
import * as randomName from 'random-name';
import {cartesianProduct, errorMessage, pickRandomly} from "./utils";
import {HttpErrorResponse} from "@angular/common/http";
import {filter} from "rxjs/internal/operators/filter";
import {KeyService, LocaleService, MessageService, ProjectService, UserService} from "@dev/translatr-sdk";

const localeNames = ['en', 'de', 'it', 'fr', 'hu', 'sl', 'cs', 'es', 'pl', 'gr'];
const featureNames = ['user', 'project', 'locale', 'key', 'message', 'accessToken'];
const keySuffixes = ['title', 'header', 'description', 'list', 'create', 'update', 'delete', 'permission'];
const keyNames = cartesianProduct([featureNames, keySuffixes]).map((values: string[]) => values.join('.'));

const createProject = (project: Project, accessToken: AccessToken, projectService: ProjectService): Observable<Project> => {
  return projectService.create(project, {params: {access_token: accessToken.key}});
};

const createLocale = (injector: Injector, locale: Locale, accessToken: AccessToken): Observable<Locale> => {
  return injector.get(LocaleService).create(locale, {params: {access_token: accessToken.key}});
};

const createKey = (injector: Injector, key: Key, accessToken: AccessToken): Observable<Key> => {
  return injector.get(KeyService).create(key, {params: {access_token: accessToken.key}});
};

const createMessage = (injector: Injector, message: Message, accessToken: AccessToken): Observable<Message> => {
  return injector.get(MessageService).create(message, {params: {access_token: accessToken.key}});
};

export const createRandomProject = (injector: Injector): Observable<Partial<State>> => {
  // Randomly choose user, create project for that user with random name
  return getRandomUserAccessToken(injector, {limit: '10'}, (user: User) => user.role === UserRole.User)
    .pipe(
      filter((payload: { user: User, accessToken: AccessToken }) =>
        !!payload.user && !!payload.user.id && !!payload.accessToken && !!payload.accessToken.key),
      concatMap((payload: { user: User, accessToken: AccessToken }) =>
        createProject(
          {
            name: randomName.place().replace(' ', ''),
            description: 'Generated',
            ownerId: payload.user.id
          },
          payload.accessToken,
          injector.get(ProjectService))
          .pipe(map((project: Project) => ({...payload, project})))
      ),
      concatMap((payload: { user: User, project: Project, accessToken: AccessToken }) => {
        return createLocale(injector,
          {
            name: pickRandomly(localeNames),
            projectId: payload.project.id
          },
          payload.accessToken
        )
          .pipe(map((locale: Locale) => ({...payload, locale})));
      }),
      concatMap((payload: { user: User, project: Project, locale: Locale, accessToken: AccessToken }) => {
        return createKey(injector,
          {
            name: pickRandomly(keyNames),
            projectId: payload.project.id
          },
          payload.accessToken
        )
          .pipe(map((key: Key) => ({...payload, key})));
      }),
      concatMap((payload: { user: User, project: Project, locale: Locale, key: Key, accessToken: AccessToken }) => {
        return createMessage(injector,
          {
            localeId: payload.locale.id,
            keyId: payload.key.id,
            value: `${payload.key.name} (${payload.locale.displayName})`
          },
          payload.accessToken
        )
          .pipe(mapTo(payload));
      }),
      map((payload: { user: User, project: Project }) =>
        ({message: `${payload.user.name} created project ${payload.project.name}`})),
      catchError((err: HttpErrorResponse) => of({message: errorMessage(err)}))
    );
};

export const updateRandomProject = (injector: Injector): Observable<Partial<State>> => {
  const projectService = injector.get(ProjectService);
  // Randomly choose user, update project of that user randomly
  return getRandomUserAccessToken(injector, {limit: '10'}, (user: User) => user.role === UserRole.User)
    .pipe(
      filter((payload: { user: User, accessToken: AccessToken }) =>
        !!payload.user && !!payload.user.id && !!payload.accessToken && !!payload.accessToken.key),
      concatMap((payload: { user: User, accessToken: AccessToken }) =>
        projectService.find(
          {owner: payload.user.id, access_token: payload.accessToken.key}
        )
          .pipe(map((pagedList: PagedList<Project>) =>
            ({user: payload.user, project: pickRandomly(pagedList.list), accessToken: payload.accessToken})))
      ),
      concatMap((payload: { user: User, project: Project, accessToken: AccessToken }) => {
        if (!!payload.project) {
          return of(payload);
        }

        const project: Project = {
          name: randomName.place().replace(' ', ''),
          description: 'Generated'
        };
        return createProject(project, payload.accessToken, projectService)
          .pipe(map((project: Project) => ({user: payload.user, project})));
      }),
      concatMap((payload: { user: User, project: Project }) => {
        return projectService.update({
          ...payload.project,
          description: payload.project.description.endsWith('!')
            ? payload.project.description.replace('!', '')
            : `${payload.project.description}!`
        });
      }),
      map((project: Project) =>
        ({message: `+++ ${project.ownerName} updated project ${project.name} +++`})),
      catchError((err: HttpErrorResponse) => of({message: errorMessage(err)}))
    )
};

export const deleteRandomProject = (injector: Injector): Observable<Partial<State>> => {
  // Randomly choose user, delete project of that user randomly
  return getRandomUser(injector.get(UserService), {limit: '10'}, (user: User) => user.role === UserRole.User)
    .pipe(map((user: User) => ({message: `TODO: ${user.name} deleted random project`})));
};
