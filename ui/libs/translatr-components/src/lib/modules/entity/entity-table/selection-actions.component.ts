import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: true,
  selector: 'selection-actions',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: '<ng-content></ng-content>'
})
export class SelectionActionsComponent {}
