import { ChangeDetectorRef, Component, Inject, Optional } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Key } from '@dev/translatr-model';
import { Observable } from 'rxjs';
import { BaseEditFormComponent } from '../edit-form/base-edit-form-component';
import { ProjectFacade } from '../project-state/+state';

interface Data {
  key: Partial<Key>;
  create?: (r: Key) => void;
  update?: (r: Key) => void;
  result$?: Observable<[Key, undefined] | [undefined, any]>;
}

@Component({
  templateUrl: './key-edit-dialog.component.html'
})
export class KeyEditDialogComponent extends BaseEditFormComponent<
  KeyEditDialogComponent,
  Key,
  Key
> {
  readonly nameFormControl = this.form.get('name');

  constructor(
    readonly snackBar: MatSnackBar,
    readonly dialogRef: MatDialogRef<KeyEditDialogComponent, Key>,
    @Optional() readonly facade: ProjectFacade,
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
      d.create ?? ((key: Key) => facade.createKey(key)),
      d.update ?? ((key: Key) => facade.updateKey(key)),
      d.result$ ?? facade.keyModified$,
      (key: Key) => `Key ${key.name} has been saved`,
      changeDetectorRef
    );
  }
}

export const openKeyEditDialog = (
  dialog: MatDialog,
  key: Partial<Key>,
  create?: (r: Key) => void,
  update?: (r: Key) => void,
  result$?: Observable<[Key, undefined] | [undefined, any]>
) => {
  return dialog.open<KeyEditDialogComponent, Data, Key>(KeyEditDialogComponent, {
    data: { key, create, update, result$ }
  });
};
