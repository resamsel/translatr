import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginPageRoutingModule } from './login-page-routing.module';
import { LoginPageComponent } from './login-page.component';
import { NavbarModule } from '@dev/translatr-components';
import { MatButtonModule, MatIconModule } from '@angular/material';

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
