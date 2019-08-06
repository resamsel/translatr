import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StoreModule } from '@ngrx/store';
import { initialState as usersInitialState, USERS_FEATURE_KEY, usersReducer } from './users.reducer';
import { EffectsModule } from '@ngrx/effects';
import { UsersEffects } from './users.effects';
import { UsersFacade } from './users.facade';

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
export class UsersModule { }
