import { ChangeDetectionStrategy, Component, Input, OnInit } from "@angular/core";
import { User } from "../../../../../../../libs/translatr-model/src/lib/model/user";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'user-card',
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
