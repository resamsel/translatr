import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import {
  AccessTokenService,
  errorMessage,
  LocaleService,
  ProjectService
} from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, filter, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { deleteRandomLocale } from '../locale';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'language',
  type: 'delete',
  name: 'Anna',
  description: "I'm going to remove a language from a random project of mine.",
  weight: 5
};

export class AnnaPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly projectService: ProjectService;
  private readonly localeService: LocaleService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.projectService = injector.get(ProjectService);
    this.localeService = injector.get(LocaleService);
  }

  execute(): Observable<string> {
    return deleteRandomLocale(
      this.accessTokenService,
      this.projectService,
      this.localeService,
      this.config.accessToken
    ).pipe(
      filter(locale => Boolean(locale)),
      map(
        locale =>
          `locale ${locale.name} of project ${locale.projectOwnerUsername}/${locale.projectName} removed`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new AnnaPersona(config, injector)
});