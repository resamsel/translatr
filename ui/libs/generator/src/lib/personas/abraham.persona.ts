import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import {
  AccessTokenService,
  errorMessage,
  LocaleService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, filter, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { createRandomLocale } from '../locale';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'language',
  type: 'create',
  name: 'Abraham',
  description: "I'm going to create a new language for a random project of mine.",
  weight: 20
};

export class AbrahamPersona extends Persona {
  private readonly userService: UserService;
  private readonly projectService: ProjectService;
  private readonly localeService: LocaleService;
  private readonly accessTokenService: AccessTokenService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.userService = injector.get(UserService);
    this.accessTokenService = injector.get(AccessTokenService);
    this.projectService = injector.get(ProjectService);
    this.localeService = injector.get(LocaleService);
  }

  execute(): Observable<string> {
    return createRandomLocale(
      this.accessTokenService,
      this.userService,
      this.projectService,
      this.localeService
    ).pipe(
      filter(locale => Boolean(locale)),
      map(
        locale =>
          `locale ${locale.displayName} for project ${locale.projectOwnerUsername}/${locale.projectName} created`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new AbrahamPersona(config, injector)
});
