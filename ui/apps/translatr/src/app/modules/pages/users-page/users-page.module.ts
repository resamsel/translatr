import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsersPageComponent } from './users-page.component';
import { UsersPageRoutingModule } from './users-page-routing.module';
import { MatIconModule } from '@angular/material';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { UserListModule } from '../../shared/user-list/user-list.module';
import { UserCardModule } from '@dev/translatr-components';
import { UsersModule } from './+state/users.module';

@NgModule({
  declarations: [UsersPageComponent],
  imports: [
    CommonModule,
    UsersPageRoutingModule,
    SidenavModule,
    UserListModule,
    UserCardModule,
    UsersModule,
    MatIconModule,
  ]
})
export class UsersPageModule {}
