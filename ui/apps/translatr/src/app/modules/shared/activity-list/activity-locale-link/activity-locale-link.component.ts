import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { Locale } from '@dev/translatr-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity-locale-link',
  templateUrl: './activity-locale-link.component.html',
  styleUrls: ['./activity-locale-link.component.scss']
})
export class ActivityLocaleLinkComponent {
  localeLink: string[] | undefined;
  @HostBinding('class.sub-title') subtitle = true;

  private _locale: Locale;

  get locale(): Locale {
    return this._locale;
  }

  @Input() set locale(locale: Locale) {
    this._locale = locale;

    if (!locale || !locale.projectOwnerUsername || !locale.projectName || !locale.name) {
      this.localeLink = undefined;
    } else {
      this.localeLink = [
        '/',
        locale.projectOwnerUsername,
        locale.projectName,
        'locales',
        locale.name
      ];
    }
  }
}
