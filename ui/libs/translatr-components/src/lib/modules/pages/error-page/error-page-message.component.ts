import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-error-page-message',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: '<ng-content></ng-content>'
})
export class ErrorPageMessageComponent {}
