import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent } from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { ProjectEmptyViewComponent } from './project-empty-view.component';

@NgModule({
  declarations: [ProjectEmptyViewComponent],
  imports: [CommonModule, EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent, MatButtonModule, TranslocoModule],
  exports: [ProjectEmptyViewComponent]
})
export class ProjectEmptyViewModule {}
