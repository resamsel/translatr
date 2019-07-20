import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar } from '@angular/material';
import { Locale } from '@dev/translatr-model';
import { LocaleService } from '@dev/translatr-sdk';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AbstractEditDialogComponent } from '../creation-dialog/abstract-edit-dialog-component';

export const openLocaleEditDialog = (dialog: MatDialog, locale: Partial<Locale>) => {
  return dialog.open<LocaleEditDialogComponent, Partial<Locale>, Locale>(
    LocaleEditDialogComponent, { data: locale });
};

@Component({
  templateUrl: './locale-edit-dialog.component.html'
})
export class LocaleEditDialogComponent
  extends AbstractEditDialogComponent<LocaleEditDialogComponent, Locale> {

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
