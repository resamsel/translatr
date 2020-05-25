import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectDeleteDialogComponent } from './project-delete-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslocoModule } from '@ngneat/transloco';


@NgModule({
  declarations: [ProjectDeleteDialogComponent],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatDialogModule,
        TranslocoModule
    ],
  entryComponents: [ProjectDeleteDialogComponent]
})
export class ProjectDeleteDialogModule {
}
