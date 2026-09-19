import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { EmptyViewComponent } from '@dev/translatr-components';
import { Project } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { filter, switchMap, take, takeUntil } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';
import { BaseEditFormComponent } from '../../../shared/edit-form/base-edit-form.component';
import { openProjectDeleteDialog } from '../../../shared/project-delete-dialog/project-delete-dialog.component';
import { ProjectFacade } from '../../../shared/project-state';

@Component({
  standalone: true,
  selector: 'app-project-settings',
  templateUrl: './project-settings.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./project-settings.component.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslocoModule,
    EmptyViewComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ]
})
export class ProjectSettingsComponent
  extends BaseEditFormComponent<ProjectSettingsComponent, Project>
  implements OnInit {
  project$ = this.appFacade.project$.pipe(filter(x => !!x));

  canDelete$ = this.facade.canDelete$;

  // Feature flag for transferring ownership
  transferOwnershipEnabled = false;

  readonly nameFormControl = this.form.get('name');
  readonly descriptionFormControl = this.form.get('description');

  constructor(
    readonly fb: FormBuilder,
    readonly snackBar: MatSnackBar,
    private readonly router: Router,
    // private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog,
    private readonly facade: ProjectFacade,
    readonly appFacade: AppFacade,
    readonly changeDetectorRef: ChangeDetectorRef
  ) {
    super(
      snackBar,
      undefined,
      fb.group({
        id: fb.control(''),
        name: fb.control('', [
          Validators.required,
          Validators.pattern('[^\\s/]+'),
          Validators.maxLength(255)
        ]),
        description: fb.control('', Validators.maxLength(2000))
      }),
      {},
      (project: Project) => appFacade.createProject(project),
      (project: Project) => appFacade.updateProject(project),
      appFacade.projectModified$,
      (project: Project) => `Project ${project.name} saved`,
      changeDetectorRef
    );
  }

  ngOnInit() {
    this.project$
      .pipe(takeUntil(this.destroy$))
      .subscribe(project => this.form.patchValue(project));
  }

  protected onSaved(project: Project): void {
    this.router.navigate(['/', project.ownerUsername, project.name, 'settings']);
  }

  onDelete() {
    this.project$
      .pipe(
        take(1),
        switchMap(project => openProjectDeleteDialog(this.dialog, project).afterClosed()),
        filter(project => !!project)
      )
      .subscribe(() => this.router.navigate(['/dashboard']));
  }
}
