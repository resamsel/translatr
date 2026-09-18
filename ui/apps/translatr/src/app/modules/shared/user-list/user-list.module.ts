import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterModule } from '@angular/router';
import { EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent, UserCardComponent, UserCardLinkComponent } from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { GravatarModule } from 'ngx-gravatar';
import { TimeAgoPipe } from '@dev/translatr-components';
import { NavListModule } from '../nav-list/nav-list.module';
import { UserListComponent } from './user-list.component';

@NgModule({
  declarations: [UserListComponent],
  imports: [
    NavListModule,
    UserCardComponent, UserCardLinkComponent,
    EmptyViewComponent, EmptyViewHeaderComponent, EmptyViewContentComponent, EmptyViewActionsComponent,

    CommonModule,
    RouterModule,
    GravatarModule,
    TranslocoModule,
    TimeAgoPipe,

    MatListModule,
    MatIconModule
  ],
  exports: [UserListComponent]
})
export class UserListModule {}
