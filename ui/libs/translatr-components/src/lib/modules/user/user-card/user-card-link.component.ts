import { Component, Input } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  selector: 'app-user-card-link',
  templateUrl: './user-card-link.component.html'
})
export class UserCardLinkComponent {
  @Input() user: User;
}
