import { CommonModule } from '@angular/common';
import { Component, Inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, Params } from '@angular/router';
import { AuthClient } from '@dev/translatr-model';
import { AuthClientService } from '@dev/translatr-sdk';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import {
  faApple,
  faFacebook,
  faGithub,
  faGoogle,
  faMicrosoft,
  faTwitter
} from '@fortawesome/free-brands-svg-icons';
import { faKey } from '@fortawesome/free-solid-svg-icons';
import { TranslocoModule } from '@jsverse/transloco';
import { ENDPOINT_URL } from '@translatr/utils';
import { combineLatest } from 'rxjs';
import { filter, map, take } from 'rxjs/operators';
import { NavbarModule } from '../../nav/navbar/navbar.module';

@Component({
  standalone: true,
  selector: 'dev-login-page',
  templateUrl: './login-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./login-page.component.scss'],
  imports: [
    CommonModule,
    NavbarModule,
    MatIconModule,
    MatButtonModule,
    FontAwesomeModule,
    MatCardModule,
    TranslocoModule
  ]
})
export class LoginPageComponent implements OnInit {
  readonly names = {
    keycloak: 'Keycloak',
    google: 'Google',
    facebook: 'Facebook',
    twitter: 'Twitter',
    github: 'GitHub',
    microsoft: 'Microsoft',
    apple: 'Apple'
  };
  icons = {
    google: faGoogle,
    keycloak: faKey,
    github: faGithub,
    facebook: faFacebook,
    twitter: faTwitter,
    microsoft: faMicrosoft,
    apple: faApple
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
    combineLatest([this.providers$, this.redirectUri$])
      .pipe(
        take(1),
        filter(([providers]) => providers.length === 1)
      )
      .subscribe(([providers, redirectUri]: [AuthClient[], string]) =>
        this.navigateTo(`${this.endpointUrl}${providers[0].url}${redirectUri}`)
      );
  }

  /**
   * Seam for tests: jsdom >= 24 makes `window.location` and its `href` non-configurable, so the
   * assignment cannot be stubbed from the outside.
   */
  navigateTo(url: string): void {
    window.location.href = url;
  }
}
