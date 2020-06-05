import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { RouterModule } from '@angular/router';
import { UserCardModule } from '@dev/translatr-components';
import { TranslocoModule } from '@ngneat/transloco';
import { GravatarModule } from 'ngx-gravatar';
import { MomentModule } from 'ngx-moment';
import { NavListModule } from '../nav-list/nav-list.module';
import { UserListComponent } from './user-list.component';

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
