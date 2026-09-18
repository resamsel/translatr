import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterModule } from '@angular/router';
import {
  EmptyViewComponent,
  EmptyViewHeaderComponent,
  EmptyViewContentComponent,
  EmptyViewActionsComponent,
  UserCardComponent,
  UserCardLinkComponent,
  TimeAgoPipe
} from '@dev/translatr-components';
import { PagedList, User } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { trackByFn } from '@translatr/utils';
import { GravatarModule } from 'ngx-gravatar';
import { FilterCriteria } from '../list-header/list-header.component';
import { NavListComponent } from '../nav-list/nav-list.component';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
  imports: [
    NavListComponent,
    UserCardComponent,
    UserCardLinkComponent,
    EmptyViewComponent,
    EmptyViewHeaderComponent,
    EmptyViewContentComponent,
    EmptyViewActionsComponent,
    CommonModule,
    RouterModule,
    GravatarModule,
    TranslocoModule,
    TimeAgoPipe,
    MatListModule,
    MatIconModule
  ]
})
export class UserListComponent {
  @Input() users: PagedList<User>;
  @Input() criteria: FilterCriteria | undefined;

  @Output() readonly filter = new EventEmitter<FilterCriteria>();

  trackByFn = trackByFn;
}
