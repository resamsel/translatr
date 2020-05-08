import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccessTokenEditFormComponent } from './access-token-edit-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { RouterModule } from '@angular/router';
import { TranslocoModule } from '@ngneat/transloco';

@NgModule({
  declarations: [AccessTokenEditFormComponent],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    TranslocoModule
  ],
  exports: [AccessTokenEditFormComponent]
})
export class AccessTokenEditFormModule {
}
