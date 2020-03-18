import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar } from '@angular/material';
import { Locale } from '@dev/translatr-model';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { BaseEditFormComponent } from '../edit-form/base-edit-form-component';
import { ProjectFacade } from '../../pages/project-page/+state/project.facade';

interface Data {
  locale: Partial<Locale>;
}

@Component({
  templateUrl: './locale-edit-dialog.component.html'
})
export class LocaleEditDialogComponent
  extends BaseEditFormComponent<LocaleEditDialogComponent, Locale> {

  readonly nameFormControl = this.form.get('name');

  constructor(
    readonly snackBar: MatSnackBar,
    readonly dialogRef: MatDialogRef<LocaleEditDialogComponent, Locale>,
    readonly facade: ProjectFacade,
    @Inject(MAT_DIALOG_DATA) readonly d: Data
  ) {
    super(
      snackBar,
      dialogRef,
      new FormGroup({
        id: new FormControl(d.locale.id),
        projectId: new FormControl(d.locale.projectId),
        name: new FormControl(d.locale.name, Validators.required)
      }),
      d.locale,
      (locale: Locale) => facade.createLocale(locale),
      (locale: Locale) => facade.updateLocale(locale),
      facade.localeModified$,
      (locale: Locale) => `Locale ${locale.name} has been saved`
    );
  }
}

export const openLocaleEditDialog = (dialog: MatDialog, locale: Partial<Locale>) => {
  return dialog.open<LocaleEditDialogComponent, Data, Locale>(
    LocaleEditDialogComponent, { data: { locale } });
};
