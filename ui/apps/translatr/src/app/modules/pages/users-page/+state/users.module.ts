import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { UsersEffects } from './users.effects';
import { UsersFacade } from './users.facade';
import { initialState as usersInitialState, USERS_FEATURE_KEY, usersReducer } from './users.reducer';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    StoreModule.forFeature(USERS_FEATURE_KEY, usersReducer, {
      initialState: usersInitialState
    }),
    EffectsModule.forFeature([UsersEffects])
  ],
  providers: [UsersFacade]
})
export class UsersModule {}
