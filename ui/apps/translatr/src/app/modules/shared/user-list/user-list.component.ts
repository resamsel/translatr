import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PagedList} from "../../../../../../../libs/translatr-sdk/src/lib/shared/paged-list";
import {User} from "../../../../../../../libs/translatr-sdk/src/lib/shared/user";

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {

  @Input() users: PagedList<User>;
  @Output() readonly more = new EventEmitter<number>();

  constructor() {
  }

  ngOnInit() {
  }

}
