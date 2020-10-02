import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { TranslocoModule } from '@ngneat/transloco';
import { UserEditFormComponent } from './user-edit-form.component';

@NgModule({
  declarations: [UserEditFormComponent],
  exports: [UserEditFormComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TranslocoModule,

    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ]
})
export class UserEditFormModule {}
