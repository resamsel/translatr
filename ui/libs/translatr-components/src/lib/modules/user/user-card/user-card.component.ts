import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss']
})
export class UserCardComponent {
  @Input() user: User;
  @HostBinding('class') clazz = 'user-card';
}
