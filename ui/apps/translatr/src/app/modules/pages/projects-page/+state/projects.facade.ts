import {Injectable} from '@angular/core';
import {select, Store} from '@ngrx/store';
import {ProjectsPartialState} from './projects.reducer';
import {projectsQuery} from './projects.selectors';
import {LoadProjects} from './projects.actions';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {ProjectCriteria} from '@dev/translatr-model';

@Injectable()
export class ProjectsFacade {
  private unload$ = new Subject<void>();

  allProjects$ = this.store.pipe(takeUntil(this.unload$.asObservable()), select(projectsQuery.getAllProjects));

  constructor(private store: Store<ProjectsPartialState>) {
  }

  loadProjects(criteria?: ProjectCriteria) {
    this.store.dispatch(new LoadProjects(criteria));
  }

  unloadProjects() {
    this.unload$.next();
  }
}
