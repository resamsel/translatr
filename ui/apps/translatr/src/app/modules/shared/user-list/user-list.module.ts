import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterModule } from '@angular/router';
import { EmptyViewModule, UserCardModule } from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { GravatarModule } from 'ngx-gravatar';
import { TimeAgoModule } from '@dev/translatr-components';
import { NavListModule } from '../nav-list/nav-list.module';
import { UserListComponent } from './user-list.component';

@NgModule({
  declarations: [UserListComponent],
  imports: [
    NavListModule,
    UserCardModule,
    EmptyViewModule,

    CommonModule,
    RouterModule,
    GravatarModule,
    TranslocoModule,
    TimeAgoModule,

    MatListModule,
    MatIconModule
  ],
  exports: [UserListComponent]
})
export class UserListModule {}
