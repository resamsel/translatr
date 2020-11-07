import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Scope } from '@dev/translatr-model';
import { AccessTokenService, errorMessage, KeyService, ProjectService } from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, concatMap, filter, map } from 'rxjs/operators';
import { chooseAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomProjectAccessToken } from '../project';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'key',
  type: 'read',
  name: 'Ferdinand',
  description: "I'm going to read all keys of a random project.",
  weight: 20
};

export class FerdinandPersona extends Persona {
  private readonly projectService: ProjectService;
  private readonly accessTokenService: AccessTokenService;
  private readonly keyService: KeyService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.projectService = injector.get(ProjectService);
    this.accessTokenService = injector.get(AccessTokenService);
    this.keyService = injector.get(KeyService);
  }

  execute(): Observable<string> {
    return selectRandomProjectAccessToken(this.accessTokenService, this.projectService).pipe(
      filter(({ project, accessToken }) => Boolean(project)),
      concatMap(({ project, accessToken }) =>
        this.keyService
          .find({
            projectId: project.id,
            access_token: chooseAccessToken(
              accessToken,
              this.config.accessToken,
              Scope.ProjectRead,
              Scope.KeyRead
            )
          })
          .pipe(
            map(paged => ({
              project,
              accessToken,
              keys: paged.list
            }))
          )
      ),
      map(
        ({ project, accessToken, keys }) =>
          `${keys.length} keys of project ${project.ownerUsername}/${project.name} viewed`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) =>
    new FerdinandPersona(config, injector)
});
