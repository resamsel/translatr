import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
  EmptyViewComponent,
  EmptyViewHeaderComponent,
  EmptyViewContentComponent,
  EmptyViewActionsComponent
} from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  standalone: true,
  selector: 'app-project-empty-view',
  templateUrl: './project-empty-view.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./project-empty-view.component.scss'],
  imports: [
    CommonModule,
    EmptyViewComponent,
    EmptyViewHeaderComponent,
    EmptyViewContentComponent,
    EmptyViewActionsComponent,
    MatButtonModule,
    TranslocoModule
  ]
})
export class ProjectEmptyViewComponent {
  @Input() canCreate = false;
  @Output() create = new EventEmitter<void>();
}
