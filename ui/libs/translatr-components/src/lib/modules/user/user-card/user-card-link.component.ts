import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { User } from '@dev/translatr-model';
import { UserCardComponent } from './user-card.component';

@Component({
  standalone: true,
  selector: 'app-user-card-link',
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './user-card-link.component.html',
  imports: [RouterModule, UserCardComponent]
})
export class UserCardLinkComponent {
  @Input() user: User;
}
