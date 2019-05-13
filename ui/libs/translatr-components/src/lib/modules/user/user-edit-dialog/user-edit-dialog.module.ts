import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserEditDialogComponent} from './user-edit-dialog.component';
import {MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule} from '@angular/material';
import {ReactiveFormsModule} from '@angular/forms';

@NgModule({
  declarations: [UserEditDialogComponent],
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatDialogModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule
  ],
  exports: [UserEditDialogComponent],
  entryComponents: [UserEditDialogComponent]
})
export class UserEditDialogModule {
}
