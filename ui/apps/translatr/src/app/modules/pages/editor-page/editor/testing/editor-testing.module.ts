import { Component, Input, NgModule } from '@angular/core';
import { Message, User } from '@dev/translatr-model';
import { Link } from '@dev/translatr-components';

@Component({
  selector: 'app-editor',
  template: ''
})
class MockEditorComponent {
  @Input() me: User;
  @Input() ownerName: string;
  @Input() projectName: string;
  @Input() name: string;
  @Input() message: Message;
  @Input() messages: Array<Message>;
  @Input() backLink: Link;
}

@Component({
  selector: 'app-editor-selector',
  template: ''
})
class MockEditorSelectorComponent {
}

@NgModule({
  declarations: [MockEditorComponent, MockEditorSelectorComponent],
  exports: [MockEditorComponent, MockEditorSelectorComponent]
})
export class EditorTestingModule {
}
