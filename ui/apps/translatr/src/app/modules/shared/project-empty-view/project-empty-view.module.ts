import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { EmptyViewModule } from '@dev/translatr-components';
import { TranslocoModule } from '@ngneat/transloco';
import { ProjectEmptyViewComponent } from './project-empty-view.component';

@NgModule({
  declarations: [ProjectEmptyViewComponent],
  imports: [CommonModule, EmptyViewModule, MatButtonModule, TranslocoModule],
  exports: [ProjectEmptyViewComponent]
})
export class ProjectEmptyViewModule {}
