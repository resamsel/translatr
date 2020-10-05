import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { AccessToken, Project, Scope } from '@dev/translatr-model';
import { AccessTokenService, ErrorHandler, ProjectService } from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { chooseAccessToken } from '../access-token';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectProjectByRandomAccessToken } from '../project';
import { Persona } from './persona';
import { personas } from './personas';

const name = 'Mila';

/**
 * I'm going to update a random project.
 */
export class MilaPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly projectService: ProjectService;
  private readonly errorHandler: ErrorHandler;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.projectService = injector.get(ProjectService);
    this.errorHandler = injector.get(ErrorHandler);
  }

  execute(): Observable<string> {
    return selectProjectByRandomAccessToken(this.accessTokenService, this.projectService).pipe(
      filter(
        (result: { project: Project; accessToken: AccessToken }) => result.project !== undefined
      ),
      switchMap((result: { project: Project; accessToken: AccessToken }) =>
        this.projectService
          .update(
            {
              ...result.project,
              name: result.project.name.endsWith('!')
                ? result.project.name.substr(0, result.project.name.length - 1)
                : `${result.project.name}!`
            },
            {
              params: {
                access_token: chooseAccessToken(
                  result.accessToken,
                  this.config.accessToken,
                  Scope.ProjectWrite
                )
              }
            }
          )
          .pipe(
            catchError((err: HttpErrorResponse) => {
              this.errorHandler.handleError(err);
              return of(undefined);
            })
          )
      ),
      map((accessToken: AccessToken) => `Project ${accessToken.name} updated`)
    );
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) => new MilaPersona(config, injector),
  weight: 5
});
