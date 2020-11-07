import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Scope } from '@dev/translatr-model';
import { AccessTokenService, errorMessage, KeyService, ProjectService } from '@dev/translatr-sdk';
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
  section: 'key',
  type: 'update',
  name: 'Hanna',
  description: "I'm going to update a key of a random project of mine.",
  weight: 10
};

const suffix = '.updated';

export class HannaPersona extends Persona {
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
    return selectRandomProjectAccessToken(this.accessTokenService, this.projectService).pipe(
      filter(({ project }) => Boolean(project)),
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
          .pipe(map(paged => ({ project, accessToken, key: pickRandomly(paged.list) })))
      ),
      filter(({ key }) => Boolean(key)),
      concatMap(({ project, accessToken, key }) =>
        this.keyService
          .update(
            {
              ...key,
              name: key.name.endsWith(suffix)
                ? key.name.replace(suffix + '$', '')
                : key.name + suffix
            },
            {
              params: {
                access_token: chooseAccessToken(
                  accessToken,
                  this.config.accessToken,
                  Scope.ProjectRead,
                  Scope.KeyWrite
                )
              }
            }
          )
          .pipe(map(k => ({ project, key: k })))
      ),
      map(
        ({ project, key }) =>
          `key ${key.name} of project ${project.ownerUsername}/${project.name} updated`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new HannaPersona(config, injector)
});
