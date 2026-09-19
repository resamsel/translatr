import { CommonModule } from '@angular/common';
import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';
import { FeatureFlagDirective, FeatureFlagClassDirective, UserEditFormComponent } from '@dev/translatr-components';
import { ConstraintViolationErrorInfo, Feature, User } from '@dev/translatr-model';
import { UserService } from '@dev/translatr-sdk';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faFacebook, faGithub, faGoogle, faTwitter } from '@fortawesome/free-brands-svg-icons';
import { faKey } from '@fortawesome/free-solid-svg-icons';
import { TranslocoModule } from '@jsverse/transloco';
import { Subject } from 'rxjs';
import { SidenavComponent } from '../../nav/sidenav/sidenav.component';

@Component({
  standalone: true,
  selector: 'app-registration-page',
  templateUrl: './registration-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./registration-page.component.scss'],
  imports: [
    CommonModule,
    SidenavComponent,
    TranslocoModule,
    FeatureFlagDirective, FeatureFlagClassDirective,
    UserEditFormComponent,
    MatCardModule,
    MatButtonModule,
    FontAwesomeModule
  ]
})
export class RegistrationPageComponent {
  readonly profile$ = this.userService.authProfile();
  readonly errors$ = new Subject<ConstraintViolationErrorInfo>();

  readonly Feature = Feature;
  readonly icons = {
    google: faGoogle,
    keycloak: faKey,
    github: faGithub,
    facebook: faFacebook,
    twitter: faTwitter
  };

  constructor(private readonly userService: UserService, private readonly router: Router) {}

  onSubmit(user: User) {
    this.userService.create(user).subscribe(
      () => this.router.navigate(['/dashboard']),
      error => this.errors$.next(error.error.error)
    );
  }
}
