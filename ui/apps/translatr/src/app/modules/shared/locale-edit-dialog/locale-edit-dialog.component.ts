import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Locale } from '@dev/translatr-model';
import { LocaleService } from '@dev/translatr-sdk';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AbstractEditFormComponent } from '../edit-form/abstract-edit-form-component';

export const openLocaleEditDialog = (dialog: MatDialog, locale: Partial<Locale>) => {
  return dialog.open<LocaleEditDialogComponent, Partial<Locale>, Locale>(
    LocaleEditDialogComponent, { data: locale });
};

@Component({
  templateUrl: './locale-edit-dialog.component.html'
})
export class LocaleEditDialogComponent
  extends AbstractEditFormComponent<LocaleEditDialogComponent, Locale> {

  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    readonly snackBar: MatSnackBar,
    readonly dialogRef: MatDialogRef<LocaleEditDialogComponent, Locale>,
    readonly localeService: LocaleService,
    @Inject(MAT_DIALOG_DATA) readonly data: Locale
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
      data,
      (locale: Locale) => localeService.create(locale),
      (locale: Locale) => localeService.update(locale),
      (locale: Locale) => `Locale ${locale.name} has been saved`
    );
  }
}
