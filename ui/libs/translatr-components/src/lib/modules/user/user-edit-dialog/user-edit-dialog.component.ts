import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {User} from "@dev/translatr-sdk";
import {FormControl, FormGroup} from "@angular/forms";

@Component({
  selector: 'dev-user-edit-dialog',
  templateUrl: './user-edit-dialog.component.html',
  styleUrls: ['./user-edit-dialog.component.css']
})
export class UserEditDialogComponent {
  form = new FormGroup({
    id: new FormControl(),
    name: new FormControl(),
    username: new FormControl(),
    email: new FormControl()
  });

  constructor(
    public dialogRef: MatDialogRef<UserEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {type: 'create' | 'update', user: User}
  ) {
    this.form.patchValue(data.user);
  }

  onCancel() {
    this.dialogRef.close();
  }
}
