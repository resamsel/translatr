import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { Message } from '@dev/translatr-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity-message-link',
  templateUrl: './activity-message-link.component.html',
  styleUrls: ['./activity-message-link.component.scss']
})
export class ActivityMessageLinkComponent {
  messageLink: string[] | undefined;
  @HostBinding('class.sub-title') subtitle = true;

  private _message: Message;

  get message(): Message {
    return this._message;
  }

  @Input() set message(message: Message) {
    this._message = message;

    this.updateLink();
  }

  private _target: 'locale' | 'key' = 'locale';

  get target(): 'locale' | 'key' {
    return this._target;
  }

  @Input() set target(value: 'locale' | 'key') {
    this._target = value;

    this.updateLink();
  }

  private updateLink(): void {
    const message = this.message;

    if (!message || !message.projectOwnerUsername || !message.projectName) {
      return;
    }

    switch (this._target) {
      case 'key':
        if (!message.keyName) {
          return;
        }

        this.messageLink = [
          '/',
          message.projectOwnerUsername,
          message.projectName,
          'keys',
          message.keyName
        ];
        break;
      case 'locale':
        if (!message.localeName) {
          return;
        }

        this.messageLink = [
          '/',
          message.projectOwnerUsername,
          message.projectName,
          'locales',
          message.localeName
        ];
        break;
    }
  }
}
