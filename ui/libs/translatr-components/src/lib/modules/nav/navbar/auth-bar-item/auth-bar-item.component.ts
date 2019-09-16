import { Component, Input } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  selector: 'app-auth-bar-item',
  templateUrl: './auth-bar-item.component.html',
  styleUrls: ['./auth-bar-item.component.scss']
})
export class AuthBarItemComponent {
  @Input() me: User;
  @Input() endpointUrl: string;
}
