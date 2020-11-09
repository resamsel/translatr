import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import {
  AccessTokenService,
  errorMessage,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, concatMap, map } from 'rxjs/operators';
import { selectRandomKeyForProject } from '../key';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomLocaleForProject } from '../locale';
import { updateMessage } from '../message';
import { selectRandomProjectAccessToken } from '../project';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'translation',
  type: 'create',
  name: 'Regina',
  description: "I'm going to translate a key for a language in a random project of mine.",
  weight: 50
};

export class ReginaPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly userService: UserService;
  private readonly projectService: ProjectService;
  private readonly localeService: LocaleService;
  private readonly keyService: KeyService;
  private readonly messageService: MessageService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.userService = injector.get(UserService);
    this.projectService = injector.get(ProjectService);
    this.localeService = injector.get(LocaleService);
    this.keyService = injector.get(KeyService);
    this.messageService = injector.get(MessageService);
  }

  execute(): Observable<string> {
    return selectRandomProjectAccessToken(
      this.accessTokenService,
      this.userService,
      this.projectService,
      this.localeService,
      this.keyService,
      this.messageService
    ).pipe(
      concatMap(({ accessToken, project }) =>
        selectRandomLocaleForProject(
          this.localeService,
          project,
          accessToken,
          this.config.accessToken
        ).pipe(map(locale => ({ accessToken, project, locale })))
      ),
      concatMap(({ accessToken, project, locale }) =>
        selectRandomKeyForProject(
          this.keyService,
          project,
          accessToken,
          this.config.accessToken
        ).pipe(map(key => ({ accessToken, project, locale, key })))
      ),
      concatMap(({ accessToken, project, locale, key }) =>
        updateMessage(
          this.messageService,
          project,
          locale,
          key,
          accessToken,
          this.config.accessToken
        ).pipe(map(() => ({ project, locale, key })))
      ),
      map(
        ({ project, locale, key }) =>
          `translation ${key.name}/${locale.name} for project ${project.ownerUsername}/${project.name} created`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new ReginaPersona(config, injector)
});
