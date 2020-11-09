import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Scope } from '@dev/translatr-model';
import {
  AccessTokenService,
  errorMessage,
  LocaleService,
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
  section: 'language',
  type: 'read',
  name: 'Sandra',
  description: "I'm going to read all languages of a random project.",
  weight: 20
};

export class SandraPersona extends Persona {
  private readonly projectService: ProjectService;
  private readonly accessTokenService: AccessTokenService;
  private readonly localeService: LocaleService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.projectService = injector.get(ProjectService);
    this.accessTokenService = injector.get(AccessTokenService);
    this.localeService = injector.get(LocaleService);
  }

  execute(): Observable<string> {
    return selectRandomProjectAccessToken(this.accessTokenService, this.projectService).pipe(
      filter(({ accessToken, project }) => Boolean(project)),
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
          .pipe(
            map(paged => ({
              project,
              accessToken,
              locales: paged.list
            }))
          )
      ),
      map(
        ({ accessToken, project, locales }) =>
          `${locales.length} languages of project ${project.ownerUsername}/${project.name} viewed`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new SandraPersona(config, injector)
});
