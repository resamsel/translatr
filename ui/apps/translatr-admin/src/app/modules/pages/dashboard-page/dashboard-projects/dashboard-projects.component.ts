import { Component, OnDestroy } from '@angular/core';
import { AppFacade } from "../../../../+state/app.facade";
import { debounceTime, distinctUntilChanged, map, mapTo, scan, shareReplay, startWith, take } from "rxjs/operators";
import { Project, ProjectCriteria, RequestCriteria } from "@dev/translatr-model";
import { merge, Observable, Subject } from "rxjs";
import { ProjectDeleted, ProjectDeleteError } from "../../../../+state/app.actions";
import { MatDialog, MatSnackBar } from "@angular/material";
import { errorMessage } from "@dev/translatr-sdk";
import { hasDeleteProjectPermission } from "@dev/translatr-sdk/src/lib/shared/permissions";
import { of } from "rxjs/internal/observable/of";
import { Entity } from "@dev/translatr-components";

@Component({
  selector: 'dev-dashboard-projects',
  templateUrl: './dashboard-projects.component.html',
  styleUrls: ['./dashboard-projects.component.css']
})
export class DashboardProjectsComponent implements OnDestroy {

  displayedColumns = ['name', 'description', 'owner', 'when_created', 'actions'];

  me$ = this.facade.me$;
  projects$ = this.facade.projects$;
  search$ = new Subject<string>();
  limit$ = new Subject<number>();
  reload$ = new Subject<void>();
  commands$ = merge(
    this.search$.asObservable().pipe(
      distinctUntilChanged(),
      debounceTime(200),
      map((search: string) => ({search}))
    ),
    this.limit$.asObservable().pipe(
      distinctUntilChanged(),
      map((limit: number) => ({limit: `${limit}`}))
    ),
    this.reload$.asObservable().pipe(mapTo({}))
  )
    .pipe(
      startWith({limit: '20', search: '', order: 'name asc'}),
      scan((acc: ProjectCriteria, value: ProjectCriteria) => ({...acc, ...value})),
      shareReplay(1)
    );

  selected: Entity[] = [];

  constructor(
    private readonly facade: AppFacade,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    this.commands$.subscribe((criteria: ProjectCriteria) => this.facade.loadProjects(criteria));
    facade.projectDeleted$
      .subscribe((action: ProjectDeleted | ProjectDeleteError) => {
        if (action instanceof ProjectDeleted) {
          snackBar.open(
            `Project ${action.payload.name} has been deleted`,
            'Dismiss',
            {duration: 3000}
          );
          this.reload$.next();
        } else {
          snackBar.open(
            `Project could not be deleted: ${errorMessage(action.payload)}`,
            'Dismiss',
            {duration: 8000}
          );
        }
      });
  }

  onSelected(entities: Entity[]) {
    this.selected = entities;
  }

  onFilter(value: string) {
    this.search$.next(value);
  }

  onLoadMore() {
    this.commands$
      .pipe(take(1))
      .subscribe((criteria: RequestCriteria) =>
        this.limit$.next(parseInt(criteria.limit, 10) * 2));
  }

  allowEdit$(project: Project): Observable<boolean> {
    return of(false); // this.me$.pipe(hasEditProjectPermission(project));
  }

  allowDelete$(project: Project): Observable<boolean> {
    return this.me$.pipe(hasDeleteProjectPermission(project));
  }

  onDelete(project: Project) {
    this.facade.deleteProject(project);
  }

  allowDeleteAll$(projects: Project[]): Observable<boolean> {
    return of(false); // this.me$.pipe(hasDeleteAllAccessTokensPermission(accessTokens));
  }

  ngOnDestroy(): void {
    this.facade.unloadProjects();
  }
}
