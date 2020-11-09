import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { Project } from '@dev/translatr-model';
import {
  AccessTokenService,
  ErrorHandler,
  KeyService,
  LocaleService,
  MessageService,
  ProjectService,
  UserService
} from '@dev/translatr-sdk';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { updateRandomProject } from '../project';
import { WeightedPersona } from '../weighted-persona';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'project',
  type: 'update',
  name: 'Mila',
  description: "I'm going to update a random project.",
  weight: 50
};

export class MilaPersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly userService: UserService;
  private readonly projectService: ProjectService;
  private readonly localeService: LocaleService;
  private readonly keyService: KeyService;
  private readonly messageService: MessageService;
  private readonly errorHandler: ErrorHandler;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.userService = injector.get(UserService);
    this.projectService = injector.get(ProjectService);
    this.localeService = injector.get(LocaleService);
    this.keyService = injector.get(KeyService);
    this.messageService = injector.get(MessageService);
    this.errorHandler = injector.get(ErrorHandler);
  }

  execute(): Observable<string> {
    return updateRandomProject(
      this.accessTokenService,
      this.userService,
      this.projectService,
      this.localeService,
      this.keyService,
      this.messageService,
      this.config.accessToken
    ).pipe(
      map((project: Project) => `project ${project.ownerUsername}/${project.name} updated`),
      catchError((err: HttpErrorResponse) => {
        this.errorHandler.handleError(err);
        return throwError(err);
      })
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new MilaPersona(config, injector)
});
