import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Key } from '@dev/translatr-model';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AbstractEditFormComponent } from '../edit-form/abstract-edit-form-component';
import { merge, Observable } from 'rxjs';
import { filter, skip } from 'rxjs/operators';

interface Data {
  key: Partial<Key>;
  creator: (Key) => void;
  updater: (Key) => void;
  result$: Observable<Key | any>;
}

@Component({
  templateUrl: './key-edit-dialog.component.html'
})
export class KeyEditDialogComponent
  extends AbstractEditFormComponent<KeyEditDialogComponent, Key> {

  readonly nameFormControl = this.form.get('name');

  constructor(
    readonly snackBar: MatSnackBar,
    readonly dialogRef: MatDialogRef<KeyEditDialogComponent, Key>,
    @Inject(MAT_DIALOG_DATA) readonly d: Data
  ) {
    super(
      snackBar,
      dialogRef,
      new FormGroup({
        id: new FormControl(d.key.id),
        projectId: new FormControl(d.key.projectId),
        name: new FormControl(d.key.name || '', [
          Validators.required,
          Validators.pattern('[^\\s/]+')
        ])
      }),
      d.key,
      (key: Key) => {
        d.creator(key);
        return d.result$;
      },
      (key: Key) => {
        d.updater(key);
        return d.result$;
      },
      (key: Key) => `Key ${key.name} has been saved`
    );
  }
}

export const openKeyEditDialog = (
  dialog: MatDialog,
  key: Partial<Key>,
  creator: (Key) => void,
  updater: (Key) => void,
  result$: Observable<Key>,
  error$: Observable<any>
) => {
  return dialog.open<KeyEditDialogComponent, Data, Key>(
    KeyEditDialogComponent, {
      data: {
        key,
        creator,
        updater,
        result$: merge(
          result$.pipe(skip(1), filter(x => !!x)),
          error$.pipe(skip(1), filter(x => !!x))
        )
      }
    });
};
