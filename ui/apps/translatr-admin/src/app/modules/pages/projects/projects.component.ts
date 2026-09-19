import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import {
  ConfirmButtonComponent,
  Entity,
  EntityTableComponent,
  notifyEvent,
  ProjectEditDialogComponent,
  SelectionActionsComponent,
  TimeAgoPipe
} from '@dev/translatr-components';
import { Feature, Project, RequestCriteria } from '@dev/translatr-model';
import {
  errorMessage,
  hasDeleteAllProjectsPermission,
  hasDeleteProjectPermission,
  hasEditProjectPermission
} from '@dev/translatr-sdk';
import { Observable, of } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import {
  AppActionTypes,
  ProjectDeleted,
  ProjectDeleteError,
  ProjectsDeleted,
  ProjectsDeleteError
} from '../../../+state/app.actions';
import { AppFacade } from '../../../+state/app.facade';
import { environment } from '../../../../environments/environment';
import { AdminPageComponent } from '../../admin-page/admin-page.component';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    AdminPageComponent,
    EntityTableComponent,
    ConfirmButtonComponent,
    SelectionActionsComponent,
    TimeAgoPipe,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule
  ]
})
export class ProjectsComponent implements OnDestroy {
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
      facade.projectDeleted$.pipe(takeUntil(facade.unloadProjects$)),
      AppActionTypes.ProjectDeleted,
      (action: ProjectDeleted) => `Project ${action.payload.name} has been deleted`,
      (action: ProjectDeleteError) =>
        `Project could not be deleted: ${errorMessage(action.payload)}`
    );
    notifyEvent(
      snackBar,
      facade.projectsDeleted$.pipe(takeUntil(facade.unloadProjects$)),
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
