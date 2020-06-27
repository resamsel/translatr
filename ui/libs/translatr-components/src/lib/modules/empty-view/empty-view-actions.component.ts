import { Component } from '@angular/core';

@Component({
  selector: 'dev-empty-view-actions',
  template: '<ng-content></ng-content>',
  styles: [
    `
      :host {
        display: block;
        padding: 16px 0;
      }
    `
  ]
})
export class EmptyViewActionsComponent {}
