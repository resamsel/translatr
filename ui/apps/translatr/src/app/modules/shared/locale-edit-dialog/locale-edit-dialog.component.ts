import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Locale } from '@dev/translatr-model';
import { LocaleService } from '@dev/translatr-sdk';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AbstractEditFormComponent } from '../edit-form/abstract-edit-form-component';

@Component({
  templateUrl: './locale-edit-dialog.component.html'
})
export class LocaleEditDialogComponent
  extends AbstractEditFormComponent<LocaleEditDialogComponent, Locale> {

  readonly nameFormControl = this.form.get('name');

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
        id: new FormControl(data.id),
        projectId: new FormControl(data.projectId),
        name: new FormControl(data.name, [
          Validators.required,
          Validators.pattern('[^\\s/]+')
        ])
      }),
      data,
      // FIXME: this works around the store, integrate it into the store instead!
      (locale: Locale) => localeService.create(locale),
      (locale: Locale) => localeService.update(locale),
      (locale: Locale) => `Locale ${locale.name} has been saved`
    );
  }
}

export const openLocaleEditDialog = (dialog: MatDialog, locale: Partial<Locale>) => {
  return dialog.open<LocaleEditDialogComponent, Partial<Locale>, Locale>(
    LocaleEditDialogComponent, { data: locale });
};
