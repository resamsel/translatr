import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectEmptyViewComponent } from './project-empty-view.component';
import { EmptyViewModule } from '@dev/translatr-components';
import { MatButtonModule } from '@angular/material';
import { TranslocoModule } from '@ngneat/transloco';

@NgModule({
  declarations: [ProjectEmptyViewComponent],
  imports: [
    CommonModule,
    EmptyViewModule,
    MatButtonModule,
    TranslocoModule
  ],
  exports: [ProjectEmptyViewComponent]
})
export class ProjectEmptyViewModule {
}
