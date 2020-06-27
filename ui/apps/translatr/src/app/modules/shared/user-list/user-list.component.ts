import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { PagedList, User } from '@dev/translatr-model';
import { trackByFn } from '@translatr/utils';
import { FilterCriteria } from '../list-header/list-header.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent {
  @Input() users: PagedList<User>;
  @Input() criteria: FilterCriteria | undefined;

  @Output() readonly filter = new EventEmitter<FilterCriteria>();

  trackByFn = trackByFn;
}
