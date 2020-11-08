import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import {
  AccessTokenService,
  errorMessage,
  MessageService,
  ProjectService
} from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, filter, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { deleteRandomMessage } from '../message';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'translation',
  type: 'delete',
  name: 'Zara',
  description: "I'm going to remove a translation from a random project of mine.",
  weight: 5
};

export class ZaraPersona extends Persona {
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
    return deleteRandomMessage(
      this.accessTokenService,
      this.projectService,
      this.messageService,
      this.config.accessToken
    ).pipe(
      filter(message => Boolean(message)),
      map(
        message =>
          `translation ${message.keyName}/${message.localeName} of project ${message.projectOwnerUsername}/${message.projectName} removed`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new ZaraPersona(config, injector)
});
