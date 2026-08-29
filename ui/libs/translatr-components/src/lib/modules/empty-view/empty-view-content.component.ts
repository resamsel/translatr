import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-empty-view-content',
  template: '<ng-content></ng-content>',
  changeDetection: ChangeDetectionStrategy.Eager,
  styles: [
    `
      :host {
        opacity: 0.6;
      }
    `
  ]
})
export class EmptyViewContentComponent {}
