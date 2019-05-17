import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginPageRoutingModule } from './login-page-routing.module';
import { LoginPageComponent } from './login-page.component';
import { MatButtonModule, MatIconModule } from '@angular/material';
import { NavbarModule } from '../../nav/navbar/navbar.module';

@NgModule({
  declarations: [LoginPageComponent],
  imports: [
    CommonModule,
    LoginPageRoutingModule,
    NavbarModule,
    MatIconModule,
    MatButtonModule
  ],
  exports: [LoginPageComponent]
})
export class LoginPageModule {
}
