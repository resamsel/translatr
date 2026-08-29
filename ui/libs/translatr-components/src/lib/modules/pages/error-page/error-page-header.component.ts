import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-error-page-header',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: '<ng-content></ng-content>'
})
export class ErrorPageHeaderComponent {}
