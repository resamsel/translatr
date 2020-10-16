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
import { LoadGeneratorConfig } from '../load-generator-config';
import { createRandomProject } from '../project';
import { Persona } from './persona';
import { personas } from './personas';

const name = 'Jerome';

/**
 * I'm going to create a new project with a few languages and a few keys.
 */
export class JeromePersona extends Persona {
  private readonly accessTokenService: AccessTokenService;
  private readonly userService: UserService;
  private readonly projectService: ProjectService;
  private readonly localeService: LocaleService;
  private readonly keyService: KeyService;
  private readonly messageService: MessageService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(name, config, injector);

    this.accessTokenService = injector.get(AccessTokenService);
    this.userService = injector.get(UserService);
    this.projectService = injector.get(ProjectService);
    this.localeService = injector.get(LocaleService);
    this.keyService = injector.get(KeyService);
    this.messageService = injector.get(MessageService);
  }

  execute(): Observable<string> {
    return createRandomProject(
      this.accessTokenService,
      this.userService,
      this.projectService,
      this.localeService,
      this.keyService,
      this.messageService
    ).pipe(
      map(
        ({ project, locales, keys }) =>
          `project ${project.ownerUsername}/${project.name} with ${locales.length} languages and ${keys.length} keys created`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  name,
  create: (config: LoadGeneratorConfig, injector: Injector) => new JeromePersona(config, injector),
  weight: 10
});
