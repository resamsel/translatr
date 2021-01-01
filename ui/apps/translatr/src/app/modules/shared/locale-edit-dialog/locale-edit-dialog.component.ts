import { ChangeDetectorRef, Component, Inject, Optional } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Locale } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { BaseEditFormComponent } from '../edit-form/base-edit-form.component';
import { ProjectFacade } from '../project-state/+state';

interface Data {
  locale: Partial<Locale>;
  create?: (r: Locale) => void;
  update?: (r: Locale) => void;
  result$?: Observable<[Locale, undefined] | [undefined, any]>;
}

@Component({
  templateUrl: './locale-edit-dialog.component.html'
})
export class LocaleEditDialogComponent extends BaseEditFormComponent<
  LocaleEditDialogComponent,
  Locale
> {
  readonly nameFormControl = this.form.get('name');

  constructor(
    readonly snackBar: MatSnackBar,
    readonly dialogRef: MatDialogRef<LocaleEditDialogComponent, Locale>,
    @Optional() readonly facade: ProjectFacade,
    readonly changeDetectorRef: ChangeDetectorRef,
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
      d.create ?? ((locale: Locale) => facade.createLocale(locale)),
      d.update ?? ((locale: Locale) => facade.updateLocale(locale)),
      d.result$ ?? facade.localeModified$,
      (locale: Locale) => `Locale ${locale.name} has been saved`,
      changeDetectorRef
    );
  }
}

export const openLocaleEditDialog = (
  dialog: MatDialog,
  locale: Partial<Locale>,
  create?: (r: Locale) => void,
  update?: (r: Locale) => void,
  result$?: Observable<[Locale, undefined] | [undefined, any]>
) => {
  return dialog.open<LocaleEditDialogComponent, Data, Locale>(LocaleEditDialogComponent, {
    data: { locale, create, update, result$ }
  });
};
