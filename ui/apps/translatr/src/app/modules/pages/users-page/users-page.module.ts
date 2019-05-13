import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UsersPageComponent} from './users-page.component';
import {UsersPageRoutingModule} from './users-page-routing.module';
import {MatIconModule} from '@angular/material';
import {SidenavModule} from '../../nav/sidenav/sidenav.module';
import {UserListModule} from '../../shared/user-list/user-list.module';
import {UserCardModule} from '@dev/translatr-components';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {initialState as usersInitialState, USERS_FEATURE_KEY, usersReducer} from './+state/users.reducer';
import {UsersEffects} from './+state/users.effects';
import {UsersFacade} from './+state/users.facade';

@NgModule({
  declarations: [UsersPageComponent],
  imports: [
    CommonModule,
    UsersPageRoutingModule,
    SidenavModule,
    UserListModule,
    UserCardModule,
    MatIconModule,
    StoreModule.forFeature(USERS_FEATURE_KEY, usersReducer, {
      initialState: usersInitialState
    }),
    EffectsModule.forFeature([UsersEffects])
  ],
  providers: [UsersFacade]
})
export class UsersPageModule {}
