import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Locale } from '@dev/translatr-model';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AbstractEditFormComponent } from '../edit-form/abstract-edit-form-component';
import { merge, Observable } from 'rxjs';
import { filter, skip } from 'rxjs/operators';

interface Data {
  locale: Partial<Locale>;
  creator: (Locale) => void;
  updater: (Locale) => void;
  result$: Observable<Locale | any>;
}

@Component({
  templateUrl: './locale-edit-dialog.component.html'
})
export class LocaleEditDialogComponent
  extends AbstractEditFormComponent<LocaleEditDialogComponent, Locale> {

  readonly nameFormControl = this.form.get('name');

  constructor(
    readonly snackBar: MatSnackBar,
    readonly dialogRef: MatDialogRef<LocaleEditDialogComponent, Locale>,
    @Inject(MAT_DIALOG_DATA) readonly d: Data
  ) {
    super(
      snackBar,
      dialogRef,
      new FormGroup({
        id: new FormControl(d.locale.id),
        projectId: new FormControl(d.locale.projectId),
        name: new FormControl(d.locale.name, [
          Validators.required,
          Validators.pattern('[^\\s/]+')
        ])
      }),
      d.locale,
      (locale: Locale) => {
        d.creator(locale);
        return d.result$;
      },
      (locale: Locale) => {
        d.updater(locale);
        return d.result$;
      },
      (locale: Locale) => `Locale ${locale.name} has been saved`
    );
  }
}

export const openLocaleEditDialog = (
  dialog: MatDialog,
  locale: Partial<Locale>,
  creator: (Locale) => void,
  updater: (Locale) => void,
  result$: Observable<Locale>,
  error$: Observable<any>
) => {
  return dialog.open<LocaleEditDialogComponent, Data, Locale>(
    LocaleEditDialogComponent, {
      data: {
        locale,
        creator,
        updater,
        result$: merge(
          result$.pipe(skip(1), filter(x => !!x)),
          error$.pipe(skip(1), filter(x => !!x))
        )
      }
    });
};
