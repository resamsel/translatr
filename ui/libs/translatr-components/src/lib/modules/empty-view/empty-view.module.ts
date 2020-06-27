import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { EmptyViewActionsComponent } from './empty-view-actions.component';
import { EmptyViewContentComponent } from './empty-view-content.component';
import { EmptyViewHeaderComponent } from './empty-view-header.component';
import { EmptyViewComponent } from './empty-view.component';

@NgModule({
  declarations: [
    EmptyViewComponent,
    EmptyViewHeaderComponent,
    EmptyViewContentComponent,
    EmptyViewActionsComponent
  ],
  exports: [
    EmptyViewComponent,
    EmptyViewHeaderComponent,
    EmptyViewContentComponent,
    EmptyViewActionsComponent
  ],
  imports: [CommonModule, MatIconModule]
})
export class EmptyViewModule {}
