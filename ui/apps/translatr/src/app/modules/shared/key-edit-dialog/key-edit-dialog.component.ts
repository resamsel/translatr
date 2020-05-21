import { ChangeDetectorRef, Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar } from '@angular/material';
import { Key } from '@dev/translatr-model';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { BaseEditFormComponent } from '../edit-form/base-edit-form-component';
import { ProjectFacade } from '../../pages/project-page/+state/project.facade';

interface Data {
  key: Partial<Key>;
}

@Component({
  templateUrl: './key-edit-dialog.component.html'
})
export class KeyEditDialogComponent
  extends BaseEditFormComponent<KeyEditDialogComponent, Key, Key> {

  readonly nameFormControl = this.form.get('name');

  constructor(
    readonly snackBar: MatSnackBar,
    readonly dialogRef: MatDialogRef<KeyEditDialogComponent, Key>,
    readonly facade: ProjectFacade,
    readonly changeDetectorRef: ChangeDetectorRef,
    @Inject(MAT_DIALOG_DATA) readonly d: Data
  ) {
    super(
      snackBar,
      dialogRef,
      new FormGroup({
        id: new FormControl(d.key.id),
        projectId: new FormControl(d.key.projectId),
        name: new FormControl(d.key.name || '', Validators.required)
      }),
      d.key,
      (key: Key) => facade.createKey(key),
      (key: Key) => facade.updateKey(key),
      facade.keyModified$,
      (key: Key) => `Key ${key.name} has been saved`,
      changeDetectorRef
    );
  }
}

export const openKeyEditDialog = (dialog: MatDialog, key: Partial<Key>) =>
  dialog.open<KeyEditDialogComponent, Data, Key>(
    KeyEditDialogComponent,
    { data: { key } }
  );
