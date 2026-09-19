import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { Link } from '@dev/translatr-components';
import { Message, User } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'app-editor',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockEditorComponent {
  @Input() me: User;
  @Input() ownerName: string;
  @Input() projectName: string;
  @Input() name: string;
  @Input() message: Message;
  @Input() messages: Array<Message>;
  @Input() backLink: Link;
}
