import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { AccessTokenService, errorMessage, KeyService, ProjectService } from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, filter, map } from 'rxjs/operators';
import { deleteRandomKey } from '../key';
import { LoadGeneratorConfig } from '../load-generator-config';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'key',
  type: 'delete',
  name: 'Laurel',
  description: "I'm going to remove a key from a random project of mine.",
  weight: 5
};

export class LaurelPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly projectService: ProjectService;
  private readonly keyService: KeyService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.projectService = injector.get(ProjectService);
    this.keyService = injector.get(KeyService);
  }

  execute(): Observable<string> {
    return deleteRandomKey(
      this.accessTokenService,
      this.projectService,
      this.keyService,
      this.config.accessToken
    ).pipe(
      filter(key => Boolean(key)),
      map(
        key => `key ${key.name} of project ${key.projectOwnerUsername}/${key.projectName} removed`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new LaurelPersona(config, injector)
});
