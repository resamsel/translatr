import { HttpErrorResponse } from '@angular/common/http';
import { Injector } from '@angular/core';
import { errorMessage, ProjectService } from '@dev/translatr-sdk';
import { WeightedPersona } from '@translatr/generator';
import { Observable, of } from 'rxjs';
import { catchError, filter, map } from 'rxjs/operators';
import { LoadGeneratorConfig } from '../load-generator-config';
import { selectRandomProject } from '../project';
import { Persona } from './persona';
import { personas } from './personas';

const info: WeightedPersona = {
  section: 'member',
  type: 'read',
  name: 'Theresa',
  description: 'I\'m going to read all contributors to a random project.',
  weight: 20
};

export class TheresaPersona extends Persona {
  private readonly projectService: ProjectService;

  constructor(config: LoadGeneratorConfig, injector: Injector) {
    super(info.name, config, injector);

    this.projectService = injector.get(ProjectService);
  }

  execute(): Observable<string> {
    return selectRandomProject(this.projectService, { fetch: 'members' }).pipe(
      filter(project => Boolean(project)),
      map(
        project =>
          `${project.members.length} members of project ${project.ownerUsername}/${project.name} viewed`
      ),
      catchError((err: HttpErrorResponse) => of(errorMessage(err)))
    );
  }
}

personas.push({
  ...info,
  create: (config: LoadGeneratorConfig, injector: Injector) => new TheresaPersona(config, injector)
});
