import { Component, Inject, ChangeDetectionStrategy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { AccessToken } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'app-protect-creation-dialog',
  templateUrl: './access-token-edit-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./access-token-edit-dialog.component.scss']
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
