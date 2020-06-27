import { Component } from '@angular/core';

@Component({
  selector: 'dev-empty-view-content',
  template: '<ng-content></ng-content>',
  styles: [
    `
      :host {
        opacity: 0.6;
      }
    `
  ]
})
export class EmptyViewContentComponent {}
