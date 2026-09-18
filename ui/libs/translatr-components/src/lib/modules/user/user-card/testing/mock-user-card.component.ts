import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'user-card',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockUserCardComponent {
  @Input() user: User;
}

@Component({
  standalone: true,
  selector: 'app-user-card-link',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockUserCardLinkComponent {
  @Input() user: User;
}
