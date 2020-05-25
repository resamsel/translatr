import { ChangeDetectorRef, Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { AbstractEditFormComponent } from '../edit-form/abstract-edit-form-component';
import { Member, MemberRole, Project } from '@dev/translatr-model';
import { Subject } from 'rxjs';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder } from '@angular/forms';
import { debounceTime, map, takeUntil } from 'rxjs/operators';
import { ProjectService } from '@dev/translatr-sdk';

interface ProjectForm {
  id: string;
  name: string;
  user: Member;
}

const modelToForm = (project: Project): ProjectForm => ({
  id: project.id,
  name: project.name,
  user: {
    id: 0,
    role: MemberRole.Owner,
    userId: project.ownerId,
    userUsername: project.ownerUsername,
    userName: project.ownerName
  }
});

const formToModel = (form: ProjectForm): Project => ({
  id: form.id,
  name: form.name,
  ownerId: form.user.userId
});

@Component({
  selector: 'app-project-owner-edit-form',
  templateUrl: './project-owner-edit-form.component.html',
  styleUrls: ['./project-owner-edit-form.component.scss']
})
export class ProjectOwnerEditFormComponent
  extends AbstractEditFormComponent<ProjectOwnerEditFormComponent, ProjectForm, Project>
  implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  @Input() set project(project: Project) {
    this.updateValue(project);
  }

  @Input() users: Member[];
  @Input() dialogRef: MatDialogRef<any, Project>;

  @Output() userFilter = new EventEmitter<string | undefined>();

  readonly userFormControl = this.form.get('user');

  constructor(
    readonly projectService: ProjectService,
    readonly snackBar: MatSnackBar,
    readonly changeDetectorRef: ChangeDetectorRef,
    readonly fb: FormBuilder,
    @Inject(MAT_DIALOG_DATA) readonly data: Project
  ) {
    super(
      snackBar,
      undefined,
      fb.group({
        id: [data.id],
        name: [data.name],
        user: [modelToForm(data).user]
      }),
      data,
      (form: ProjectForm) => projectService.create(formToModel(form)),
      (form: ProjectForm) => projectService.update(formToModel(form)),
      (project: Project) => `${project.ownerName} is now the new owner`
    );
  }

  ngOnInit(): void {
    this.failure.pipe(takeUntil(this.destroy$))
      .subscribe(() => this.changeDetectorRef.markForCheck());
    this.userFormControl.valueChanges
      .pipe(
        debounceTime(200),
        map(value => typeof value === 'string' ? value : value.username),
        takeUntil(this.destroy$)
      )
      .subscribe(value => this.userFilter.emit(value));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  displayFn(user?: Member): string | undefined {
    return user ? user.userUsername : undefined;
  }

  get dirty(): boolean {
    return this.form.dirty;
  }

  private updateValue(project: Project): void {
    this.form.patchValue(modelToForm(project));
  }
}
