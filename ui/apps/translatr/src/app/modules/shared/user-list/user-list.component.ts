import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PagedList, RequestCriteria, User } from '@dev/translatr-model';
import { trackByFn } from '@translatr/utils';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent {
  @Input() users: PagedList<User>;
  @Input() criteria: RequestCriteria | undefined;

  @Output() readonly more = new EventEmitter<number>();
  @Output() readonly filter = new EventEmitter<string>();

  trackByFn = trackByFn;
}
