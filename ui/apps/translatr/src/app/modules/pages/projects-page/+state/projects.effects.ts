import {Injectable} from '@angular/core';
import {Actions, Effect} from '@ngrx/effects';
import {DataPersistence} from '@nrwl/nx';
import {ProjectsPartialState} from './projects.reducer';
import {LoadProjects, ProjectsActionTypes, ProjectsLoaded, ProjectsLoadError} from './projects.actions';
import {ProjectService} from '@dev/translatr-sdk';
import {map} from 'rxjs/operators';
import {PagedList, Project} from '@dev/translatr-model';

@Injectable()
export class ProjectsEffects {
  @Effect() loadProjects$ = this.dataPersistence.fetch(
    ProjectsActionTypes.LoadProjects,
    {
      run: (action: LoadProjects, state: ProjectsPartialState) => {
        return this.projectService.find(action.payload)
          .pipe(map((payload: PagedList<Project>) => new ProjectsLoaded(payload)));
      },

      onError: (action: LoadProjects, error) => {
        console.error('Error', error);
        return new ProjectsLoadError(error);
      }
    }
  );

  constructor(
    private actions$: Actions,
    private dataPersistence: DataPersistence<ProjectsPartialState>,
    private readonly projectService: ProjectService
  ) {
  }
}
