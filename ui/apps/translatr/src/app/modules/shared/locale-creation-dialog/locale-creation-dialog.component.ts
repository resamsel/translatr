import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar } from '@angular/material';
import { Locale } from '@dev/translatr-model';
import { LocaleService } from '@dev/translatr-sdk';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AbstractCreationDialogComponent } from '../creation-dialog/abstract-creation-dialog-component';

export const openLocaleCreationDialog = (dialog: MatDialog, projectId: string) => {
  return dialog.open
  < LocaleCreationDialogComponent, { projectId: string }, Locale >
  (LocaleCreationDialogComponent, { data: { projectId } });
};

@Component({
  templateUrl: './locale-creation-dialog.component.html'
})
export class LocaleCreationDialogComponent
  extends AbstractCreationDialogComponent<LocaleCreationDialogComponent, Locale> {

  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    readonly snackBar: MatSnackBar,
    readonly dialogRef: MatDialogRef<LocaleCreationDialogComponent, Locale>,
    readonly localeService: LocaleService,
    @Inject(MAT_DIALOG_DATA) readonly data: { projectId: string }
  ) {
    super(
      snackBar,
      dialogRef,
      new FormGroup({
        projectId: new FormControl(data.projectId),
        name: new FormControl('', [
          Validators.required,
          Validators.pattern('[^\\s/]+')
        ])
      }),
      (locale: Locale) => localeService.create(locale),
      (locale: Locale) => `Locale ${locale.name} has been created`
    );
  }
}
