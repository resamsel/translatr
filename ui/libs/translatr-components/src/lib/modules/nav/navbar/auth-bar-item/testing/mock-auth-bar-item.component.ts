import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'app-auth-bar-item',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockAuthBarItemComponent {
  @Input() me: User;
  @Input() endpointUrl: string;
}
