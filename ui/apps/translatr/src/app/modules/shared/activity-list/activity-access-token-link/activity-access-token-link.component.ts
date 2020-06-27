import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { AccessToken } from '@dev/translatr-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity-access-token-link',
  templateUrl: './activity-access-token-link.component.html',
  styleUrls: ['./activity-access-token-link.component.scss']
})
export class ActivityAccessTokenLinkComponent {
  accessTokenLink: string[] | undefined;
  @HostBinding('class.sub-title') subtitle = true;

  private _accessToken: AccessToken;

  get accessToken(): AccessToken {
    return this._accessToken;
  }

  @Input() set accessToken(accessToken: AccessToken) {
    this._accessToken = accessToken;

    if (!accessToken || !accessToken.userUsername) {
      this.accessTokenLink = undefined;
    } else {
      this.accessTokenLink = [
        '/',
        accessToken.userUsername,
        'access-tokens',
        accessToken.id.toString()
      ];
    }
  }
}
