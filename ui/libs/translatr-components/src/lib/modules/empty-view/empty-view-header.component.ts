import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-empty-view-header',
  template: '<ng-content></ng-content>',
  changeDetection: ChangeDetectionStrategy.Eager,
  styles: [
    `
      :host {
        display: block;
        opacity: 0.6;
        font-size: 16px;
        line-height: 28px;
        margin-bottom: 8px;
      }
    `
  ]
})
export class EmptyViewHeaderComponent {}
