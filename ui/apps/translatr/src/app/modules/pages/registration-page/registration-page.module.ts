import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { FeatureFlagModule, UserEditFormModule } from '@dev/translatr-components';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faFacebook, faGithub, faGoogle, faTwitter } from '@fortawesome/free-brands-svg-icons';
import { faKey } from '@fortawesome/free-solid-svg-icons';
import { TranslocoModule } from '@ngneat/transloco';
import { SidenavModule } from '../../nav/sidenav/sidenav.module';
import { RegistrationPageRoutingModule } from './registration-page-routing.module';
import { RegistrationPageComponent } from './registration-page.component';

@NgModule({
  declarations: [RegistrationPageComponent],
  imports: [
    CommonModule,

    RegistrationPageRoutingModule,
    SidenavModule,
    TranslocoModule,
    FeatureFlagModule,
    UserEditFormModule,
    MatCardModule,
    MatButtonModule,
    FontAwesomeModule
  ]
})
export class RegistrationPageModule {
  constructor(readonly library: FaIconLibrary) {
    library.addIcons(faGoogle, faGithub, faFacebook, faTwitter, faKey);
  }
}
