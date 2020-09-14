import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faFacebook, faGithub, faGoogle, faTwitter } from '@fortawesome/free-brands-svg-icons';
import { faKey } from '@fortawesome/free-solid-svg-icons';
import { TranslocoModule } from '@ngneat/transloco';
import { NavbarModule } from '../../nav/navbar/navbar.module';
import { LoginPageRoutingModule } from './login-page-routing.module';
import { LoginPageComponent } from './login-page.component';

@NgModule({
  declarations: [LoginPageComponent],
  imports: [
    CommonModule,
    LoginPageRoutingModule,
    NavbarModule,
    MatIconModule,
    MatButtonModule,
    FontAwesomeModule,
    MatCardModule,
    TranslocoModule
  ],
  exports: [LoginPageComponent]
})
export class LoginPageModule {
  constructor(readonly library: FaIconLibrary) {
    library.addIcons(faGoogle, faGithub, faFacebook, faTwitter, faKey);
  }
}
