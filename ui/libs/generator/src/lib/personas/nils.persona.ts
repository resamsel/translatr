import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Scope } from '@dev/translatr-model';
import {
  AccessTokenService,
  errorMessage,
  MessageService,
  ProjectService
} from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, concatMap, filter, map } from 'rxjs/operators';
import { chooseAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomProjectAccessToken } from '../project';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'translation',
  type: 'read',
  name: 'Nils',
  description: "I'm going to read all translations of a random project.",
  weight: 20
};

export class NilsPersona extends Persona {
  private readonly projectService: ProjectService;
  private readonly accessTokenService: AccessTokenService;
  private readonly messageService: MessageService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.projectService = injector.get(ProjectService);
    this.accessTokenService = injector.get(AccessTokenService);
    this.messageService = injector.get(MessageService);
  }

  execute(): Observable<string> {
    return selectRandomProjectAccessToken(this.accessTokenService, this.projectService).pipe(
      filter(({ project, accessToken }) => Boolean(project)),
      concatMap(({ project, accessToken }) =>
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
          .pipe(
            map(paged => ({
              project,
              accessToken,
              messages: paged.list
            }))
          )
      ),
      map(
        ({ project, accessToken, messages }) =>
          `${messages.length} translations in project ${project.ownerUsername}/${project.name} viewed`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new NilsPersona(config, injector)
});
