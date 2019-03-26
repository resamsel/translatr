import {Component} from "@angular/core";

@Component({
  selector: 'app-editor-selector',
  template: '<ng-content></ng-content>',
  styles: [`:host {
    min-height: 52px;
    display: flex;
    flex-flow: row nowrap;
    align-items: center;
    padding: 0 8px;
  }
  `]
})
export class EditorSelectorComponent {
}
