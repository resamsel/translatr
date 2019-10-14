import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, Validators } from '@angular/forms';
import { filter } from 'rxjs/operators';
import { ProjectFacade } from '../+state/project.facade';
import { AbstractEditFormComponent } from '../../../shared/edit-form/abstract-edit-form-component';
import { Project } from '@dev/translatr-model';
import { MatSnackBar } from '@angular/material';
import { ProjectService } from '@dev/translatr-sdk';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-project-settings',
  templateUrl: './project-settings.component.html',
  styleUrls: ['./project-settings.component.scss']
})
export class ProjectSettingsComponent
  extends AbstractEditFormComponent<ProjectSettingsComponent, Project>
  implements OnInit {

  project$ = this.facade.project$.pipe(filter(x => !!x));

  constructor(
    readonly fb: FormBuilder,
    readonly snackBar: MatSnackBar,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly facade: ProjectFacade,
    private readonly projectService: ProjectService
  ) {
    super(
      snackBar,
      undefined,
      fb.group({
        'id': fb.control(''),
        'name': fb.control('',
          [Validators.required, Validators.maxLength(255)]),
        'description': fb.control('', Validators.maxLength(2000))
      }),
      {},
      (project: Project) => this.projectService.create(project),
      (project: Project) => this.projectService.update(project),
      (project: Project) => `Project ${project.name} saved`
    );
  }

  get nameFormControl(): AbstractControl {
    return this.form.get('name');
  }

  get descriptionFormControl(): AbstractControl {
    return this.form.get('description');
  }

  ngOnInit() {
    this.project$.subscribe((project) => this.form.patchValue(project));
  }

  onDelete() {
    // TODO: implement!
  }

  protected onSaved(project: Project): void {
    if (project.name !== this.route.snapshot.params.projectName) {
      this.router.navigate(['/', project.ownerUsername, project.name, 'settings']);
    }
    this.facade.projectLoaded(project);
  }
}
