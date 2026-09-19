import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: true,
  selector: 'app-editor-selector',
  template: '<ng-content></ng-content>',
  changeDetection: ChangeDetectionStrategy.Eager,
  styles: [
    `
      :host {
        min-height: 52px;
        display: flex;
        flex-flow: row nowrap;
        align-items: center;
        padding: 0 8px;
      }
    `
  ]
})
export class EditorSelectorComponent {}
