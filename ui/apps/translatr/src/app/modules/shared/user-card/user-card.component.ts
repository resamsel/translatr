import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { User } from "../../../../../../../libs/translatr-sdk/src/lib/shared/user";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss'],
  host: {
    class: 'user-card'
  }
})
export class UserCardComponent implements OnInit {

  @Input() user: User;

  constructor() { }

  ngOnInit() {
  }

}