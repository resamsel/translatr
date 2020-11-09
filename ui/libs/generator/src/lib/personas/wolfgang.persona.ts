import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Scope } from '@dev/translatr-model';
import {
  AccessTokenService,
  errorMessage,
  MessageService,
  ProjectService
} from '@dev/translatr-sdk';
import { pickRandomly } from '@translatr/utils';
import { Observable, of } from 'rxjs';
import { catchError, concatMap, filter, map } from 'rxjs/operators';
import { chooseAccessToken } from '../access-token';
import { messageSuffix } from '../constants';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomProjectAccessToken } from '../project';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'translation',
  type: 'update',
  name: 'Wolfgang',
  description: "I'm going to update a translation of a random project of mine.",
  weight: 50
};

export class WolfgangPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly projectService: ProjectService;
  private readonly messageService: MessageService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.projectService = injector.get(ProjectService);
    this.messageService = injector.get(MessageService);
  }

  execute(): Observable<string> {
    return selectRandomProjectAccessToken(this.accessTokenService, this.projectService, {
      fetch: 'members'
    }).pipe(
      filter(({ project }) => Boolean(project)),
      concatMap(({ accessToken, project }) =>
        this.messageService
          .find({
            projectId: project.id,
            access_token: chooseAccessToken(
              accessToken,
              this.config.accessToken,
              Scope.ProjectRead,
              Scope.MessageRead
            )
          })
          .pipe(map(paged => ({ accessToken, project, message: pickRandomly(paged.list) })))
      ),
      filter(({ message }) => Boolean(message)),
      concatMap(({ accessToken, project, message }) =>
        this.messageService
          .update(
            {
              ...message,
              value: message.value.endsWith(messageSuffix)
                ? message.value.replace(messageSuffix + '$', '')
                : message.value + messageSuffix
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
          .pipe(map(m => ({ project, message: m })))
      ),
      map(
        ({ project, message }) =>
          `translation ${message.keyName}/${message.localeName} of project ${project.ownerUsername}/${project.name} updated`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new WolfgangPersona(config, injector)
});
