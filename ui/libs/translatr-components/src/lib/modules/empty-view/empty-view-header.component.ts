import { Component } from '@angular/core';

@Component({
  selector: 'dev-empty-view-header',
  template: '<ng-content></ng-content>',
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
