import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ConstraintViolationErrorInfo, Feature, User } from '@dev/translatr-model';
import { UserService } from '@dev/translatr-sdk';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-registration-page',
  templateUrl: './registration-page.component.html',
  styleUrls: ['./registration-page.component.scss']
})
export class RegistrationPageComponent {
  readonly profile$ = this.userService.authProfile();
  readonly errors$ = new Subject<ConstraintViolationErrorInfo>();

  readonly Feature = Feature;
  readonly icons = {
    google: ['fab', 'google'],
    keycloak: ['fas', 'key'],
    github: ['fab', 'github'],
    facebook: ['fab', 'facebook'],
    twitter: ['fab', 'twitter']
  };

  constructor(private readonly userService: UserService, private readonly router: Router) {}

  onSubmit(user: User) {
    this.userService.create(user).subscribe(
      () => {
        console.log('navigating to dashboard');
        this.router.navigate(['/dashboard']);
      },
      error => this.errors$.next(error.error.error)
    );
  }
}
