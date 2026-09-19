import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../../../guards/auth.guard';
import { UsersModule } from './+state/users.module';
import { UsersPageComponent } from './users-page.component';

const routes: Routes = [
  {
    path: '',
    component: UsersPageComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes), UsersModule],
  exports: [RouterModule]
})
export class UsersPageRoutingModule {}
