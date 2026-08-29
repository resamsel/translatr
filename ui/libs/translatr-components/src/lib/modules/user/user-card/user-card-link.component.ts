import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'app-user-card-link',
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './user-card-link.component.html'
})
export class UserCardLinkComponent {
  @Input() user: User;
}
