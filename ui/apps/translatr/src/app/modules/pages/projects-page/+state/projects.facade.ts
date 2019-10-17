import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { ProjectsPartialState } from './projects.reducer';
import { projectsQuery } from './projects.selectors';
import { LoadMyProjects, LoadProjects } from './projects.actions';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { PagedList, Project, ProjectCriteria } from '@dev/translatr-model';

@Injectable()
export class ProjectsFacade {
  private unload$ = new Subject<void>();

  myProjects$: Observable<PagedList<Project>> = this.store.pipe(
    select(projectsQuery.getMyProjects),
    takeUntil(this.unload$)
  );

  projects$: Observable<PagedList<Project>> = this.store.pipe(
    select(projectsQuery.getProjects),
    takeUntil(this.unload$)
  );

  constructor(private store: Store<ProjectsPartialState>) {}

  loadProjects(criteria?: ProjectCriteria) {
    this.store.dispatch(new LoadProjects(criteria));
  }

  loadMyProjects(criteria?: ProjectCriteria) {
    this.store.dispatch(new LoadMyProjects(criteria));
  }

  unloadProjects() {
    this.unload$.next();
  }
}
