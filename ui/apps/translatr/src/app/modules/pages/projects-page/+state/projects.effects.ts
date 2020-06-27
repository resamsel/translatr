import { Injectable } from '@angular/core';
import { PagedList, Project } from '@dev/translatr-model';
import { ProjectService } from '@dev/translatr-sdk';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import {
  LoadMyProjects,
  LoadProjects,
  MyProjectsLoaded,
  MyProjectsLoadError,
  ProjectsActionTypes,
  ProjectsLoaded,
  ProjectsLoadError
} from './projects.actions';

@Injectable()
export class ProjectsEffects {
  loadProjects$ = createEffect(() =>
    this.actions$.pipe(
      ofType<LoadProjects>(ProjectsActionTypes.LoadProjects),
      switchMap((action: LoadProjects) =>
        this.projectService.find(action.payload).pipe(
          map((payload: PagedList<Project>) => new ProjectsLoaded(payload)),
          catchError(error => of(new ProjectsLoadError(error)))
        )
      )
    )
  );

  loadMyProjects$ = createEffect(() =>
    this.actions$.pipe(
      ofType<LoadMyProjects>(ProjectsActionTypes.LoadMyProjects),
      switchMap((action: LoadMyProjects) =>
        this.projectService.find(action.payload).pipe(
          map((payload: PagedList<Project>) => new MyProjectsLoaded(payload)),
          catchError(error => of(new MyProjectsLoadError(error)))
        )
      )
    )
  );

  constructor(private actions$: Actions, private readonly projectService: ProjectService) {}
}
