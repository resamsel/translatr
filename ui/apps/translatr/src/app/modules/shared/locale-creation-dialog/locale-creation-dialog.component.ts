import { Component, ElementRef, HostListener, Inject, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar } from '@angular/material';
import { ConstraintViolation, Error, Locale } from '@dev/translatr-model';
import { LocaleService } from '@dev/translatr-sdk';
import { FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { take } from 'rxjs/operators';

const ENTER_KEYCODE = 'Enter';

export const openLocaleCreationDialog = (dialog: MatDialog, projectId: string) => {
  return dialog.open
  < LocaleCreationDialogComponent, { projectId: string }, Locale >
  (LocaleCreationDialogComponent, { data: { projectId } });
};

@Component({
  templateUrl: './locale-creation-dialog.component.html',
})
export class LocaleCreationDialogComponent {
  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    private readonly snackBar: MatSnackBar,
    private readonly localeService: LocaleService,
    private readonly dialogRef: MatDialogRef<LocaleCreationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private readonly data: { projectId: string }
  ) {
  }

  @ViewChild('name') nameField: ElementRef;

  form = new FormGroup({
    projectId: new FormControl(this.data.projectId),
    name: new FormControl('', [
      Validators.required,
      Validators.pattern('[^\\s/]+')
    ])
  });
  public processing = false;

  log = console.log;

  public onSave(): void {
    this.processing = true;
    this.localeService
      .create(this.form.value)
      .pipe(take(1))
      .subscribe(
        (locale: Locale) => this.onCreated(locale),
        (res: { error: Error }) => {
          this.processing = false;

          this.nameFormControl.setErrors(
            res.error.error.violations.reduce(
              (prev: ValidationErrors, violation: ConstraintViolation) => ({
                ...prev,
                [violation.field]: violation.message
              }),
              {}
            )
          );
          this.nameFormControl.markAsTouched();
        }
      );
  }

  private onCreated(locale: Locale): void {
    this.processing = false;
    this.dialogRef.close(locale);
    this.snackBar.open(
      `Locale ${locale.name} has been created`,
      'Dismiss',
      { duration: 3000 }
    );
  }

  @HostListener('window:keyup', ['$event'])
  public onHotkey(event: KeyboardEvent) {
    if (event.key === ENTER_KEYCODE && this.form.valid && !this.processing) {
      this.onSave();
    }
  }
}
