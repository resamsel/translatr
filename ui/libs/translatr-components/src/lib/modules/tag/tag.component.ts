import { Component } from '@angular/core';

@Component({
  selector: 'app-tag',
  template: '<ng-content></ng-content>',
  styles: [
    `
      :host {
        background-color: rgba(0, 0, 0, 0.08);
        border-radius: 4px;
        padding: 4px;
        font-weight: 500;
      }
    `
  ]
})
export class TagComponent {}
