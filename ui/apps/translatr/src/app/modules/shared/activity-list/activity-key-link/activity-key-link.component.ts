import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { Key } from '@dev/translatr-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity-key-link',
  templateUrl: './activity-key-link.component.html',
  styleUrls: ['./activity-key-link.component.scss']
})
export class ActivityKeyLinkComponent {
  keyLink: string[] | undefined;
  @HostBinding('class.sub-title') subtitle = true;

  private _key: Key;

  get key(): Key {
    return this._key;
  }

  @Input() set key(key: Key) {
    this._key = key;

    if (!key || !key.projectOwnerUsername || !key.projectName || !key.name) {
      this.keyLink = undefined;
    } else {
      this.keyLink = ['/', key.projectOwnerUsername, key.projectName, 'keys', key.name];
    }
  }
}
