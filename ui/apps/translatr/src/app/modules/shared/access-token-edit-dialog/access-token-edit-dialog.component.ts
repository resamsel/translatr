import { CommonModule } from '@angular/common';
import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { AccessToken } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { AccessTokenEditFormComponent } from '../access-token-edit-form/access-token-edit-form.component';

@Component({
  standalone: true,
  selector: 'app-protect-creation-dialog',
  templateUrl: './access-token-edit-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./access-token-edit-dialog.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatButtonModule,
    MatInputModule,
    AccessTokenEditFormComponent,
    TranslocoModule
  ]
})
export class AccessTokenEditDialogComponent {
  constructor(
    readonly dialogRef: MatDialogRef<AccessTokenEditDialogComponent, AccessToken>,
    @Inject(MAT_DIALOG_DATA) readonly data: AccessToken
  ) {}
}

export const openAccessTokenEditDialog = (dialog: MatDialog, accessToken: Partial<AccessToken>) => {
  return dialog.open<AccessTokenEditDialogComponent, Partial<AccessToken>, AccessToken>(
    AccessTokenEditDialogComponent,
    { data: accessToken }
  );
};
