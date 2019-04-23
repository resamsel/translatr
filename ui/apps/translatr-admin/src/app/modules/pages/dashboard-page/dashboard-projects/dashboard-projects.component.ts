import {Component, OnDestroy} from '@angular/core';
import {AppFacade} from "../../../../+state/app.facade";
import {Project, RequestCriteria} from "@dev/translatr-model";
import {Observable, of} from "rxjs";
import {ProjectDeleted, ProjectDeleteError} from "../../../../+state/app.actions";
import {MatDialog, MatSnackBar} from "@angular/material";
import {errorMessage} from "@dev/translatr-sdk";
import {hasDeleteProjectPermission} from "@dev/translatr-sdk/src/lib/shared/permissions";
import {Entity} from "@dev/translatr-components";

@Component({
  selector: 'dev-dashboard-projects',
  templateUrl: './dashboard-projects.component.html',
  styleUrls: ['./dashboard-projects.component.css']
})
export class DashboardProjectsComponent implements OnDestroy {

  displayedColumns = ['name', 'description', 'owner', 'when_created', 'actions'];

  me$ = this.facade.me$;
  projects$ = this.facade.projects$;
  load$ = of({limit: '20', order: 'name asc'});

  selected: Entity[] = [];

  constructor(
    private readonly facade: AppFacade,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    facade.projectDeleted$
      .subscribe((action: ProjectDeleted | ProjectDeleteError) => {
        if (action instanceof ProjectDeleted) {
          snackBar.open(
            `Project ${action.payload.name} has been deleted`,
            'Dismiss',
            {duration: 3000}
          );
          // this.reload$.next();
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

  onCriteriaChanged(criteria: RequestCriteria) {
    this.facade.loadProjects(criteria);
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
