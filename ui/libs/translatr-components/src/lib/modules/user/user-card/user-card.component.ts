import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { User } from '@dev/translatr-model';
import { GravatarModule } from 'ngx-gravatar';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss'],
  imports: [MatCardModule, GravatarModule]
})
export class UserCardComponent {
  @Input() user: User;
  @HostBinding('class') clazz = 'user-card';
}
