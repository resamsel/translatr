import { Component, Input } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'app-auth-bar-item',
  template: ''
})
export class MockAuthBarItemComponent {
  @Input() me: User;
  @Input() endpointUrl: string;
}
