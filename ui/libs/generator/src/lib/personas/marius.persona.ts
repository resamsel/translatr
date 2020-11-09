import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Scope } from '@dev/translatr-model';
import {
  AccessTokenService,
  errorMessage,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import { Observable, of } from 'rxjs';
import { catchError, concatMap, filter, map } from 'rxjs/operators';
import { chooseAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomProjectAccessToken } from '../project';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'language',
  type: 'update',
  name: 'Marius',
  description: "I'm going to update a language of a random project of mine.",
  weight: 10
};

export class MariusPersona extends Persona {
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
      this.messageService,
      {
        fetch: 'members'
      }
    ).pipe(
      concatMap(({ accessToken, project }) =>
        this.localeService
          .find({
            projectId: project.id,
            access_token: chooseAccessToken(
              accessToken,
              this.config.accessToken,
              Scope.ProjectRead,
              Scope.LocaleRead
            )
          })
          .pipe(map(paged => ({ accessToken, project, locale: pickRandomly(paged.list) })))
      ),
      filter(({ locale }) => Boolean(locale)),
      concatMap(({ accessToken, project, locale }) =>
        this.localeService
          .update(
            {
              ...locale,
              name: locale.name.endsWith('_formal')
                ? locale.name.replace(/_formal$/, '')
                : locale.name + '_formal'
            },
            {
              params: {
                access_token: chooseAccessToken(
                  accessToken,
                  this.config.accessToken,
                  Scope.ProjectRead,
                  Scope.LocaleWrite
                )
              }
            }
          )
          .pipe(map(l => ({ project, locale: l })))
      ),
      map(
        ({ project, locale }) =>
          `locale ${locale.name} of project ${project.ownerUsername}/${project.name} updated`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new MariusPersona(config, injector)
});
