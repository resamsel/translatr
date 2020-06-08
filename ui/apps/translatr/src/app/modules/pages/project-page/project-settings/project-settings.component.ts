import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Project } from '@dev/translatr-model';
import { filter, skip, switchMap, take, takeUntil } from 'rxjs/operators';
import { ProjectFacade } from '../+state/project.facade';
import { AppFacade } from '../../../../+state/app.facade';
import { BaseEditFormComponent } from '../../../shared/edit-form/base-edit-form-component';
import { openProjectDeleteDialog } from '../../../shared/project-delete-dialog/project-delete-dialog.component';

@Component({
  selector: 'app-project-settings',
  templateUrl: './project-settings.component.html',
  styleUrls: ['./project-settings.component.scss']
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
      appFacade.projectModified$.pipe(skip(1)),
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
