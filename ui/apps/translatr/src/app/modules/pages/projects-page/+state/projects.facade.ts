import {Injectable} from '@angular/core';

import {select, Store} from '@ngrx/store';

import {ProjectsPartialState} from './projects.reducer';
import {projectsQuery} from './projects.selectors';
import {LoadProjects, UnloadProjects} from './projects.actions';
import {Project} from "../../../../../../../../libs/translatr-sdk/src/lib/shared/project";
import {PagedList} from "../../../../../../../../libs/translatr-sdk/src/lib/shared/paged-list";
import {Observable, Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";

@Injectable()
export class ProjectsFacade {
  get allProjects$(): Observable<PagedList<Project>> {
    return this.store.pipe(takeUntil(this.unload$), select(projectsQuery.getAllProjects));
  }

  get unload$(): Observable<void> {
    return this._unload$.asObservable();
  }

  private _unload$ = new Subject<void>();

  constructor(private store: Store<ProjectsPartialState>) {
  }

  loadProjects() {
    this.store.dispatch(new LoadProjects());
  }

  unloadProjects() {
    this._unload$.next();
    this.store.dispatch(new UnloadProjects());
  }
}
