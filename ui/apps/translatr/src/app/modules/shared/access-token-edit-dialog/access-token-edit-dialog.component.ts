import { Component, Inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar } from '@angular/material';
import { AccessTokenService } from '@dev/translatr-sdk';
import { AccessToken } from '@dev/translatr-model';
import { AbstractEditDialogComponent } from '../creation-dialog/abstract-edit-dialog-component';

export const openAccessTokenEditDialog = (dialog: MatDialog, accessToken: Partial<AccessToken>) => {
  return dialog.open<AccessTokenEditDialogComponent, Partial<AccessToken>, AccessToken>(
    AccessTokenEditDialogComponent, { data: accessToken });
};

@Component({
  selector: 'app-protect-creation-dialog',
  templateUrl: './access-token-edit-dialog.component.html',
  styleUrls: ['./access-token-edit-dialog.component.scss']
})
export class AccessTokenEditDialogComponent
  extends AbstractEditDialogComponent<AccessTokenEditDialogComponent, AccessToken> {
  public get nameFormControl() {
    return this.form.get('name');
  }

  constructor(
    readonly snackBar: MatSnackBar,
    readonly accessTokenService: AccessTokenService,
    readonly dialogRef: MatDialogRef<AccessTokenEditDialogComponent, AccessToken>,
    @Inject(MAT_DIALOG_DATA) readonly data: AccessToken
  ) {
    super(
      snackBar,
      dialogRef,
      new FormGroup({
        name: new FormControl('', [
          Validators.required,
          Validators.pattern('[^\\s/]+')
        ])
      }),
      { ...data, name: data.name || '' },
      (accessToken: AccessToken) => accessTokenService.create(accessToken),
      (accessToken: AccessToken) => accessTokenService.update(accessToken),
      (accessToken: AccessToken) => `AccessToken ${accessToken.name} has been created`
    );
  }
}
