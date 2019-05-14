import { Component, Input } from '@angular/core';
import { User } from '@dev/translatr-model';

@Component({
  selector: 'app-auth-bar-item',
  templateUrl: './auth-bar-item.component.html',
  styleUrls: ['./auth-bar-item.component.scss']
})
export class AuthBarItemComponent {
  private _me: User;

  @Input() set me(me: User) {
    console.log('me', me);
    this._me = me;
  }
  @Input() endpointUrl: string;

  get me(): User {
    return this._me;
  }
}
