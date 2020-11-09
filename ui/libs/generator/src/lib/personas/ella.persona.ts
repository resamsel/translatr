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
import { catchError, map } from 'rxjs/operators';
import { createRandomKey } from '../key';
import { LoadGeneratorConfig } from '../load-generator-config';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'key',
  type: 'create',
  name: 'Ella',
  description: "I'm going to create a new key for a random project of mine.",
  weight: 20
};

export class EllaPersona extends Persona {
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
    return createRandomKey(
      this.accessTokenService,
      this.userService,
      this.projectService,
      this.localeService,
      this.keyService,
      this.messageService,
      this.config.accessToken
    ).pipe(
      map(
        key => `key ${key.name} for project ${key.projectOwnerUsername}/${key.projectName} created`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new EllaPersona(config, injector)
});
