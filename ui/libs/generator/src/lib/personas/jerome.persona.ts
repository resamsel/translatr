import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { AccessToken, Key, Locale, Project, Scope, User } from '@dev/translatr-model';
import {
  AccessTokenService,
  ErrorHandler,
  errorMessage,
  KeyService,
  LocaleService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import * as randomName from 'random-name';
import { combineLatest, Observable, of } from 'rxjs';
import { catchError, concatMap, map, switchMap } from 'rxjs/operators';
import * as _ from 'underscore';
import { chooseAccessToken } from '../access-token';
import { keyNames } from '../key';
import { LoadGeneratorConfig } from '../load-generator-config';
import { localeNames } from '../locale';
import { selectUserByRandomAccessToken } from '../user';
import { Persona } from './persona';
import { personas } from './personas';

const name = 'Jerome';

/**
 * I'm going to create a new project with a few languages and a few keys.
 */
export class JeromePersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly userService: UserService;
  private readonly projectService: ProjectService;
  private readonly localeService: LocaleService;
  private readonly keyService: KeyService;
  private readonly errorHandler: ErrorHandler;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.userService = injector.get(UserService);
    this.projectService = injector.get(ProjectService);
    this.localeService = injector.get(LocaleService);
    this.keyService = injector.get(KeyService);
    this.errorHandler = injector.get(ErrorHandler);
  }

  execute(): Observable<string> {
    return selectUserByRandomAccessToken(this.accessTokenService, this.userService).pipe(
      switchMap((result: { user: User; accessToken: AccessToken }) =>
        this.projectService
          .create(
            {
              ownerId: result.user.id,
              name: randomName.place().replace(/[^a-zA-Z0-9]/),
              description: 'Generated'
            },
            {
              params: {
                access_token: chooseAccessToken(
                  result.accessToken,
                  this.config.accessToken,
                  Scope.ProjectWrite
                )
              }
            }
          )
          .pipe(map(project => ({ ...result, project })))
      ),
      switchMap((result: { user: User; accessToken: AccessToken; project: Project }) =>
        combineLatest(
          _.sample(localeNames, Math.ceil(Math.random() * localeNames.length))
            .filter(localeName => localeName !== undefined && localeName !== '')
            .map((localeName: string) =>
              this.localeService
                .create(
                  {
                    name: localeName,
                    projectId: result.project.id
                  },
                  {
                    params: {
                      access_token: chooseAccessToken(
                        result.accessToken,
                        this.config.accessToken,
                        Scope.ProjectWrite,
                        Scope.LocaleWrite
                      )
                    }
                  }
                )
                .pipe(
                  catchError((err: HttpErrorResponse) => {
                    this.errorHandler.handleError(err);
                    return of(undefined);
                  })
                )
            )
        ).pipe(
          map((locales: Locale[]) => ({
            ...result,
            locales: locales.filter(locale => locale !== undefined)
          }))
        )
      ),
      concatMap(
        (result: { user: User; accessToken: AccessToken; project: Project; locales: Locale[] }) => {
          return combineLatest(
            _.sample(keyNames, Math.ceil((Math.random() * keyNames.length) / 10))
              .filter(keyName => keyName !== undefined && keyName !== '')
              .map((keyName: string) =>
                this.keyService
                  .create(
                    {
                      name: keyName,
                      projectId: result.project.id
                    },
                    {
                      params: {
                        access_token: chooseAccessToken(
                          result.accessToken,
                          this.config.accessToken,
                          Scope.ProjectWrite,
                          Scope.KeyWrite
                        )
                      }
                    }
                  )
                  .pipe(
                    catchError((err: HttpErrorResponse) => {
                      this.errorHandler.handleError(err);
                      return of(undefined);
                    })
                  )
              )
          ).pipe(map((keys: Key[]) => ({ ...result, keys })));
        }
      ),
      map(
        ({ project, locales, keys }) =>
          `project ${project.name} with ${locales.length} locales and ${keys.length} keys created`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) => new JeromePersona(config, injector),
  weight: 5
});
