import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Key } from '@dev/translatr-model';
import { KeyService } from '@dev/translatr-sdk';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AbstractEditFormComponent } from '../edit-form/abstract-edit-form-component';

@Component({
  templateUrl: './key-edit-dialog.component.html'
})
export class KeyEditDialogComponent
  extends AbstractEditFormComponent<KeyEditDialogComponent, Key> {

  readonly nameFormControl = this.form.get('name');

  constructor(
    readonly snackBar: MatSnackBar,
    readonly dialogRef: MatDialogRef<KeyEditDialogComponent, Key>,
    readonly keyService: KeyService,
    @Inject(MAT_DIALOG_DATA) readonly data: Key
  ) {
    super(
      snackBar,
      dialogRef,
      new FormGroup({
        projectId: new FormControl(data.projectId),
        name: new FormControl(data.name || '', [
          Validators.required,
          Validators.pattern('[^\\s/]+')
        ])
      }),
      data,
      (key: Key) => keyService.create(key),
      (key: Key) => keyService.update(key),
      (key: Key) => `Key ${key.name} has been saved`
    );
  }
}

export const openKeyEditDialog = (dialog: MatDialog, key: Partial<Key>) => {
  return dialog.open<KeyEditDialogComponent, Partial<Key>, Key>(
    KeyEditDialogComponent, { data: key });
};
