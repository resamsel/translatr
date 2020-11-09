import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Scope } from '@dev/translatr-model';
import {
  AccessTokenService,
  errorMessage,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService
} from '@dev/translatr-sdk';
import { keyNames } from '../constants';
import { combineLatest, Observable, of } from 'rxjs';
import { catchError, concatMap, filter, map } from 'rxjs/operators';
import _ from 'underscore';
import { chooseAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectLocaleForProject } from '../locale';
import { selectRandomProjectAccessToken } from '../project';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'translation',
  type: 'create',
  name: 'Una',
  description:
    "I'm going to create new keys and translate them for language English in a random project of mine.",
  weight: 30
};

export class UnaPersona extends Persona {
  private readonly projectService: ProjectService;
  private readonly localeService: LocaleService;
  private readonly keyService: KeyService;
  private readonly accessTokenService: AccessTokenService;
  private readonly messageService: MessageService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.projectService = injector.get(ProjectService);
    this.localeService = injector.get(LocaleService);
    this.keyService = injector.get(KeyService);
    this.messageService = injector.get(MessageService);
  }

  execute(): Observable<string> {
    return selectRandomProjectAccessToken(this.accessTokenService, this.projectService).pipe(
      filter(({ project }) => Boolean(project)),
      concatMap(({ accessToken, project }) =>
        selectLocaleForProject(
          'en',
          this.localeService,
          project,
          accessToken,
          this.config.accessToken
        ).pipe(map(locale => ({ accessToken, project, locale })))
      ),
      concatMap(({ accessToken, project, locale }) =>
        // Retrieve all existing keys
        this.keyService
          .find({
            projectId: project.id,
            localeId: locale.id,
            limit: 200,
            access_token: chooseAccessToken(
              accessToken,
              this.config.accessToken,
              Scope.ProjectRead,
              Scope.KeyRead
            )
          })
          .pipe(
            map(paged => ({
              project,
              accessToken,
              locale,
              newKeyNames: _.sample(
                _.difference(
                  keyNames,
                  paged.list.map(key => key.name)
                ),
                Math.ceil((Math.random() * keyNames.length) / 10)
              ) as string[]
            }))
          )
      ),
      concatMap(({ accessToken, project, locale, newKeyNames }) =>
        // Create
        combineLatest(
          newKeyNames.map(keyName =>
            this.keyService
              .create(
                { projectId: project.id, name: keyName },
                {
                  params: {
                    access_token: chooseAccessToken(
                      accessToken,
                      this.config.accessToken,
                      Scope.ProjectRead,
                      Scope.KeyWrite
                    )
                  }
                }
              )
              .pipe(catchError(() => of(undefined)))
          )
        ).pipe(map(keys => ({ accessToken, project, locale, keys: keys.filter(Boolean) })))
      ),
      concatMap(({ accessToken, project, locale, keys }) =>
        // Translate created keys for given language
        combineLatest(
          keys.map(key =>
            this.messageService
              .create(
                {
                  projectId: project.id,
                  localeId: locale.id,
                  keyId: key.id,
                  value: `${key.name} (${locale.displayName})`
                },
                {
                  params: {
                    access_token: chooseAccessToken(
                      accessToken,
                      this.config.accessToken,
                      Scope.ProjectRead,
                      Scope.MessageWrite
                    )
                  }
                }
              )
              .pipe(catchError(() => of(undefined)))
          )
        ).pipe(
          map(messages => ({
            project,
            accessToken,
            locale,
            keys,
            messages: messages.filter(Boolean)
          }))
        )
      ),
      map(
        ({ project, locale, messages }) =>
          `${messages.length} translations for language ${locale.name} for project ${project.ownerUsername}/${project.name} created`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new UnaPersona(config, injector)
});
