import { Component, ElementRef, HostListener, NgZone, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup, ValidationErrors, Validators } from "@angular/forms";
import { MatDialogRef, MatSnackBar } from "@angular/material";
import { ProjectService } from "../../../services/project.service";
import { Project } from "../../../shared/project";

const ENTER_KEYCODE = 'Enter';

interface Violation {
  message: string;
  field: string;
  invalidValue: object
}

interface Error {
  error: {
    type: string;
    message: string;
    violations: Violation[];
  }
}

@Component({
  selector: 'app-protect-creation-dialog',
  templateUrl: './project-creation-dialog.component.html',
  styleUrls: ['./project-creation-dialog.component.scss']
})
export class ProjectCreationDialogComponent implements OnInit {
  @ViewChild('name') nameField: ElementRef;

  form = new FormGroup({
    'name': new FormControl('', [
      Validators.required,
      Validators.pattern('[^\\s/]+')
    ])
  });
  public processing: boolean = false;

  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    private readonly snackBar: MatSnackBar,
    private readonly projectService: ProjectService,
    private readonly dialogRef: MatDialogRef<ProjectCreationDialogComponent>
  ) {
  }

  ngOnInit() {
    this.nameFormControl
      .statusChanges
      .subscribe(status => console.log('status', status, this.processing));
  }

  public onSave(): void {
    this.processing = true;
    this.projectService.create(this.form.value)
      .subscribe(
        () => this.onCreated(this.form.value),
        (res: { error: Error }) => {
          console.error(res);
          this.processing = false;

          this.nameFormControl
            .setErrors(res.error.error.violations.reduce(
              (prev: ValidationErrors, violation: Violation) => ({...prev, [violation.field]: violation.message}),
              {}));
          this.nameFormControl.markAsTouched();
        }
      );
  }

  log = console.log;

  private onCreated(project: Project): void {
    this.processing = false;
    this.dialogRef.close();
    this.snackBar.open(
      `Project ${project.name} has been created`,
      'Dismiss',
      {duration: 3000}
    );
  }

  @HostListener('window:keyup', ['$event'])
  public onHotkey(event: KeyboardEvent) {
    if (event.key === ENTER_KEYCODE && this.form.valid && !this.processing) {
      this.onSave();
    }
  }
}
