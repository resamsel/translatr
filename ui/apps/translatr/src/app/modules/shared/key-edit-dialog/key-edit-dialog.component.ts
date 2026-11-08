import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Inject, Optional, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Key } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { Observable } from 'rxjs';
import { BaseEditFormComponent } from '../edit-form/base-edit-form.component';
import { ProjectStateModule } from '../project-state';
import { ProjectFacade } from '../project-state/+state';

interface Data {
  key: Partial<Key>;
  create?: (r: Key) => void;
  update?: (r: Key) => void;
  result$?: Observable<[Key, undefined] | [undefined, any]>;
}

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './key-edit-dialog.component.html',
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ProjectStateModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule,
    TranslocoModule
  ]
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
