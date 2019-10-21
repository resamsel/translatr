import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { filter, switchMap, take } from 'rxjs/operators';
import { ProjectFacade } from '../+state/project.facade';
import { AbstractEditFormComponent } from '../../../shared/edit-form/abstract-edit-form-component';
import { Project } from '@dev/translatr-model';
import { MatDialog, MatSnackBar } from '@angular/material';
import { ProjectService } from '@dev/translatr-sdk';
import { ActivatedRoute, Router } from '@angular/router';
import { openProjectDeleteDialog } from '../../../shared/project-delete-dialog/project-delete-dialog.component';

@Component({
  selector: 'app-project-settings',
  templateUrl: './project-settings.component.html',
  styleUrls: ['./project-settings.component.scss']
})
export class ProjectSettingsComponent
  extends AbstractEditFormComponent<ProjectSettingsComponent, Project>
  implements OnInit {

  project$ = this.facade.project$.pipe(filter(x => !!x));

  canDelete$ = this.facade.canDelete$;

  // Feature flag for transferring ownership
  transferOwnershipEnabled = false;

  readonly nameFormControl = this.form.get('name');
  readonly descriptionFormControl = this.form.get('description');

  constructor(
    readonly fb: FormBuilder,
    readonly snackBar: MatSnackBar,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog,
    private readonly facade: ProjectFacade,
    private readonly projectService: ProjectService
  ) {
    super(
      snackBar,
      undefined,
      fb.group({
        'id': fb.control(''),
        'name': fb.control('', [
          Validators.required,
          Validators.pattern('[^\\s/]+'),
          Validators.maxLength(255)
        ]),
        'description': fb.control('', Validators.maxLength(2000))
      }),
      {},
      (project: Project) => this.projectService.create(project),
      (project: Project) => this.projectService.update(project),
      (project: Project) => `Project ${project.name} saved`
    );
  }

  ngOnInit() {
    this.project$.subscribe((project) => this.form.patchValue(project));
  }

  onDelete() {
    this.project$
      .pipe(
        take(1),
        switchMap((project) =>
          openProjectDeleteDialog(this.dialog, project).afterClosed()),
        filter(project => !!project)
      )
      .subscribe(() => this.router.navigate(['/dashboard']));
  }

  protected onSaved(project: Project): void {
    if (project.name !== this.route.snapshot.params.projectName) {
      this.router.navigate(['/', project.ownerUsername, project.name, 'settings']);
    }
    this.facade.projectLoaded(project);
  }
}
