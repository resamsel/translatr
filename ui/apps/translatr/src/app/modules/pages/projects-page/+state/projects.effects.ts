import {Injectable} from '@angular/core';
import {Actions, Effect} from '@ngrx/effects';
import {DataPersistence} from '@nrwl/nx';

import {PROJECTS_FEATURE_KEY, ProjectsPartialState} from './projects.reducer';
import {LoadProjects, ProjectsActionTypes, ProjectsLoaded, ProjectsLoadError} from './projects.actions';
import {ProjectService} from "../../../../services/project.service";
import {map} from "rxjs/operators";
import {PagedList} from "../../../../shared/paged-list";
import {Project} from "../../../../shared/project";

@Injectable()
export class ProjectsEffects {
  @Effect() loadProjects$ = this.dataPersistence.fetch(
    ProjectsActionTypes.LoadProjects,
    {
      run: (action: LoadProjects, state: ProjectsPartialState) => {
        if (!action.payload.reload && state[PROJECTS_FEATURE_KEY].pagedList) {
          console.log('Reusing cache');
          return new ProjectsLoaded(state[PROJECTS_FEATURE_KEY].pagedList);
        }
        // Your custom REST 'load' logic goes here. For now just return an empty list...
        return this.projectService.getProjects({params: {order: 'whenUpdated desc'}})
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
