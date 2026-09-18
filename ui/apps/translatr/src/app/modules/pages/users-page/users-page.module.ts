import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { FeatureFlagDirective, FeatureFlagClassDirective, UserCardComponent, UserCardLinkComponent } from '@dev/translatr-components';
import { TranslocoModule } from '@jsverse/transloco';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { UserListModule } from '../../shared/user-list/user-list.module';
import { UsersModule } from './+state/users.module';
import { UsersPageRoutingModule } from './users-page-routing.module';
import { UsersPageComponent } from './users-page.component';

@NgModule({
  declarations: [UsersPageComponent],
  imports: [
    CommonModule,
    UsersPageRoutingModule,
    SidenavModule,
    UserListModule,
    UserCardComponent, UserCardLinkComponent,
    UsersModule,
    MatIconModule,
    FeatureFlagDirective, FeatureFlagClassDirective,
    TranslocoModule
  ]
})
export class UsersPageModule {}
