import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { LoadProjects, ProjectsActionTypes, ProjectsLoaded, ProjectsLoadError } from './projects.actions';
import { ProjectService } from '@dev/translatr-sdk';
import { catchError, map, switchMap } from 'rxjs/operators';
import { PagedList, Project } from '@dev/translatr-model';
import { of } from 'rxjs';

@Injectable()
export class ProjectsEffects {
  @Effect() loadProjects$ = this.actions$.pipe(
    ofType<LoadProjects>(ProjectsActionTypes.LoadProjects),
    switchMap((action: LoadProjects) =>
      this.projectService
        .find(action.payload)
        .pipe(
          map((payload: PagedList<Project>) => new ProjectsLoaded(payload)),
          catchError(error => of(new ProjectsLoadError(error)))
        )
    )
  );

  constructor(
    private actions$: Actions,
    private readonly projectService: ProjectService
  ) {
  }
}
