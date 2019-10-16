import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectDeleteDialogComponent } from './project-delete-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule } from '@angular/material';


@NgModule({
  declarations: [ProjectDeleteDialogComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule
  ],
  entryComponents: [ProjectDeleteDialogComponent]
})
export class ProjectDeleteDialogModule {
}
