import { Component, Inject, OnInit } from '@angular/core';
import { ENDPOINT_URL } from '@translatr/utils';
import { AuthProviderService } from '@translatr/translatr-sdk/src/lib/services/auth-provider.service';
import { filter, take } from 'rxjs/operators';
import { AuthProvider } from '@dev/translatr-model';

@Component({
  selector: 'dev-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.scss']
})
export class LoginPageComponent implements OnInit {

  readonly names = {
    'keycloak': 'Keycloak',
    'google': 'Google',
    'facebook': 'Facebook',
    'twitter': 'Twitter',
    'github': 'GitHub'
  };

  readonly providers$ = this.authProviderService.find();

  constructor(
    private readonly authProviderService: AuthProviderService,
    @Inject(ENDPOINT_URL) public readonly endpointUrl: string
  ) {
  }

  ngOnInit() {
    this.providers$
      .pipe(
        take(1),
        filter(providers => providers.length === 1)
      )
      .subscribe((providers: Array<AuthProvider>) =>
        window.location.href = `${this.endpointUrl}${providers[0].url}`
      );
  }
}
