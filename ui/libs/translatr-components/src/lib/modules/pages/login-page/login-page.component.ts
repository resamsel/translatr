import { Component, Inject, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { AuthClient } from '@dev/translatr-model';
import { AuthClientService } from '@dev/translatr-sdk';
import { ENDPOINT_URL } from '@translatr/utils';
import { filter, map, take } from 'rxjs/operators';

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
  icons = {
    google: ['fab', 'google'],
    keycloak: ['fas', 'key'],
    github: ['fab', 'github'],
    facebook: ['fab', 'facebook'],
    twitter: ['fab', 'twitter']
  };

  readonly providers$ = this.authProviderService
    .find()
    .pipe(map(clients => clients.filter(client => this.names[client.key] !== undefined)));

  readonly redirectUri$ = this.route.queryParams.pipe(
    map((params: Params) =>
      params.redirect_uri !== undefined ? '?redirect_uri=' + params.redirect_uri : ''
    )
  );

  constructor(
    private readonly authProviderService: AuthClientService,
    private readonly route: ActivatedRoute,
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
