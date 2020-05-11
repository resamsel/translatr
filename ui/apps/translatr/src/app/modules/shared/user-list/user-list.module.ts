import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserListComponent } from './user-list.component';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { GravatarModule } from 'ngx-gravatar';
import { MomentModule } from 'ngx-moment';
import { RouterModule } from '@angular/router';
import { NavListModule } from '../nav-list/nav-list.module';
import { UserCardModule } from '@dev/translatr-components';
import { TranslocoModule } from '@ngneat/transloco';

@NgModule({
  declarations: [UserListComponent],
  imports: [
    CommonModule,
    RouterModule,
    NavListModule,
    UserCardModule,
    MatListModule,
    MatIconModule,
    GravatarModule,
    MomentModule,
    TranslocoModule
  ],
  exports: [UserListComponent]
})
export class UserListModule {}
