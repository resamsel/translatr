import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar } from '@angular/material';
import { Key } from '@dev/translatr-model';
import { KeyService } from '@dev/translatr-sdk';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AbstractCreationDialogComponent } from '../creation-dialog/abstract-creation-dialog-component';

export const openKeyCreationDialog = (dialog: MatDialog, projectId: string) => {
  return dialog.open<KeyCreationDialogComponent, { projectId: string }, Key>(KeyCreationDialogComponent, { data: { projectId } });
};

@Component({
  templateUrl: './key-creation-dialog.component.html'
})
export class KeyCreationDialogComponent
  extends AbstractCreationDialogComponent<KeyCreationDialogComponent, Key> {

  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    readonly snackBar: MatSnackBar,
    readonly dialogRef: MatDialogRef<KeyCreationDialogComponent, Key>,
    readonly keyService: KeyService,
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
      (key: Key) => keyService.create(key),
      (key: Key) => `Key ${key.name} has been created`
    );
  }
}
