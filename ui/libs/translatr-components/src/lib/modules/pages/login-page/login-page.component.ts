import { Component, Inject, OnInit } from '@angular/core';
import { AuthClient } from '@dev/translatr-model';
import { AuthClientService } from '@translatr/translatr-sdk/src/lib/services/auth-client.service';
import { ENDPOINT_URL } from '@translatr/utils';
import { filter, take } from 'rxjs/operators';

@Component({
  selector: 'dev-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.scss']
})
export class LoginPageComponent implements OnInit {
  readonly names = {
    keycloak: 'Keycloak',
    google: 'Google',
    facebook: 'Facebook',
    twitter: 'Twitter',
    github: 'GitHub'
  };

  readonly providers$ = this.authProviderService.find();

  constructor(
    private readonly authProviderService: AuthClientService,
    @Inject(ENDPOINT_URL) public readonly endpointUrl: string
  ) {}

  ngOnInit() {
    this.providers$
      .pipe(
        take(1),
        filter(providers => providers.length === 1)
      )
      .subscribe(
        (providers: AuthClient[]) =>
          (window.location.href = `${this.endpointUrl}${providers[0].url}`)
      );
  }
}
