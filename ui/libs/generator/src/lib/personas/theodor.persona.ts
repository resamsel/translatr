import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Project } from '@dev/translatr-model';
import { AccessTokenService, ErrorHandler, ProjectService, UserService } from '@dev/translatr-sdk';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { deleteRandomProject } from '../project';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'project',
  type: 'delete',
  name: 'Theodor',
  description: "I'm going to delete a random project.",
  weight: 2
};

export class TheodorPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly projectService: ProjectService;
  private readonly userService: UserService;
  private readonly errorHandler: ErrorHandler;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.userService = injector.get(UserService);
    this.projectService = injector.get(ProjectService);
    this.errorHandler = injector.get(ErrorHandler);
  }

  execute(): Observable<string> {
    return deleteRandomProject(this.accessTokenService, this.userService, this.projectService).pipe(
      map((project: Project) => `project ${project.ownerUsername}/${project.name} deleted`),
      catchError((err: HttpErrorResponse) => {
        this.errorHandler.handleError(err);
        return throwError(err);
      })
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new TheodorPersona(config, injector)
});
