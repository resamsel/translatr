import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Entity, notifyEvent, ProjectEditDialogComponent } from '@dev/translatr-components';
import { Feature, Project, RequestCriteria } from '@dev/translatr-model';
import {
  errorMessage,
  hasDeleteAllProjectsPermission,
  hasDeleteProjectPermission,
  hasEditProjectPermission
} from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import {
  AppActionTypes,
  ProjectDeleted,
  ProjectDeleteError,
  ProjectsDeleted,
  ProjectsDeleteError
} from '../../../../+state/app.actions';
import { AppFacade } from '../../../../+state/app.facade';
import { environment } from '../../../../../environments/environment';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-projects',
  templateUrl: './dashboard-projects.component.html',
  styleUrls: ['./dashboard-projects.component.scss']
})
export class DashboardProjectsComponent implements OnDestroy {
  displayedColumns = ['name', 'description', 'owner', 'when_created', 'actions'];

  me$ = this.facade.me$;
  projects$ = this.facade.projects$;
  load$ = of({ limit: '20', order: 'whenCreated desc' });

  filters = [
    {
      key: 'search',
      type: 'string',
      title: 'search',
      value: ''
    },
    {
      key: 'owner',
      type: 'string',
      title: 'project.owner',
      value: ''
    }
  ];

  selected: Project[] = [];

  readonly uiUrl = environment.uiUrl;

  readonly Feature = Feature;

  constructor(
    private readonly facade: AppFacade,
    private readonly dialog: MatDialog,
    readonly snackBar: MatSnackBar
  ) {
    notifyEvent(
      snackBar,
      facade.projectDeleted$,
      AppActionTypes.ProjectDeleted,
      (action: ProjectDeleted) => `Project ${action.payload.name} has been deleted`,
      (action: ProjectDeleteError) =>
        `Project could not be deleted: ${errorMessage(action.payload)}`
    );
    notifyEvent(
      snackBar,
      facade.projectsDeleted$,
      AppActionTypes.ProjectsDeleted,
      (action: ProjectsDeleted) => `${action.payload.length} projects have been deleted`,
      (action: ProjectsDeleteError) =>
        `Projects could not be deleted: ${errorMessage(action.payload)}`
    );
  }

  onSelected(entities: Entity[]) {
    this.selected = entities as Project[];
  }

  onCriteriaChanged(criteria: RequestCriteria) {
    this.facade.loadProjects(criteria);
  }

  allowEdit$(project: Project): Observable<boolean> {
    return this.me$.pipe(hasEditProjectPermission(project));
  }

  onEdit(project: Project) {
    this.dialog.open(ProjectEditDialogComponent, {
      data: {
        type: 'update',
        project,
        onSubmit: (p: Project) => this.facade.updateProject(p),
        success$: this.facade.projectUpdated$,
        error$: this.facade.projectUpdateError$
      }
    });
  }

  allowDelete$(project: Project): Observable<boolean> {
    return this.me$.pipe(hasDeleteProjectPermission(project));
  }

  onDelete(project: Project) {
    this.facade.deleteProject(project);
  }

  allowDeleteAll$(projects: Project[]): Observable<boolean> {
    return this.me$.pipe(hasDeleteAllProjectsPermission(projects));
  }

  onDeleteAll(projects: Project[]) {
    this.facade.deleteProjects(projects);
  }

  ngOnDestroy(): void {
    this.facade.unloadProjects();
  }
}
