import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccessTokenEditFormComponent } from './access-token-edit-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatInputModule } from '@angular/material';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [AccessTokenEditFormComponent],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule
  ],
  exports: [AccessTokenEditFormComponent]
})
export class AccessTokenEditFormModule {
}
