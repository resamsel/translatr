import { ChangeDetectionStrategy, Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { AccessToken, Activity, Key, Locale, Member, Message, PagedList, Project } from '@dev/translatr-model';
import { FilterCriteria } from '../list-header/list-header.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity-list',
  templateUrl: './activity-list.component.html',
  styleUrls: ['./activity-list.component.scss'],
  preserveWhitespaces: true
})
export class ActivityListComponent {
  @Input() activities: PagedList<Activity>;
  @Input() showMore = true;
  @Input() relativeTo: 'user' | 'project' | undefined;

  @Output() filter = new EventEmitter<FilterCriteria>();
  @Output() more = new EventEmitter<number>();

  @HostBinding('class') clazz = 'activity-list';

  trackByFn(index, item: Activity): string {
    return item.id;
  }

  onFilter(criteria: FilterCriteria): void {
    this.filter.emit(criteria);
  }

  iconType(contentType: string) {
    switch (contentType) {
      case 'dto.ProjectUser':
        return 'member';
      default:
        return contentType.substr(contentType.indexOf('.') + 1).toLowerCase();
    }
  }

  constructor() {
  }

  icon(contentType: string) {
    switch (contentType) {
      case 'dto.Project':
        return 'view_quilt';
      case 'dto.Locale':
        return 'language';
      case 'dto.Key':
        return 'vpn_key';
      case 'dto.Message':
        return 'message';
      case 'dto.ProjectUser':
        return 'group';
      case 'dto.User':
        return 'account_circle';
      default:
        return 'vpn_key';
    }
  }

  contentTypeOf(activity: Activity | undefined): string | undefined {
    if (!activity) {
      return undefined;
    }

    return activity.contentType.replace('dto.', '').toLowerCase();
  }

  projectOf(e: Locale | Key | Message | Member): Project | undefined {
    if (!e || !e.projectOwnerUsername || !e.projectName) {
      return undefined;
    }

    return {
      id: e.projectId,
      ownerUsername: e.projectOwnerUsername,
      name: e.projectName
    };
  }

  project(activity: Activity | undefined): Project | undefined {
    return this.parse(activity);
  }

  locale(activity: Activity | undefined): Locale | undefined {
    return this.parse(activity);
  }

  key(activity: Activity | undefined): Key | undefined {
    return this.parse(activity);
  }

  message(activity: Activity | undefined): Message | undefined {
    return this.parse(activity);
  }

  member(activity: Activity | undefined): Member | undefined {
    return this.parse(activity);
  }

  accessToken(activity: Activity | undefined): AccessToken | undefined {
    return this.parse(activity);
  }

  parse<T>(activity: Activity | undefined): T | undefined {
    if (!activity || (!activity.after && !activity.before)) {
      return undefined;
    }

    if (activity.after) {
      return JSON.parse(activity.after);
    }

    // for deleted objects
    return JSON.parse(activity.before);
  }

  linkToKeyMessage(message: Message): string[] | undefined {
    return this.linkToMessage(message, 'keys', message.keyName);
  }

  linkToLocaleMessage(message: Message): string[] | undefined {
    return this.linkToMessage(message, 'locales', message.localeName);
  }

  linkToMessage(message: Message, subpath: string, name: string): string[] | undefined {
    if (!message || !message.projectOwnerUsername || !message.projectName || !name) {
      return undefined;
    }

    return [
      '/',
      message.projectOwnerUsername,
      message.projectName,
      subpath,
      name
    ];
  }

  linkToAccessToken(accessToken: AccessToken | undefined): string[] | undefined {
    if (!accessToken || !accessToken.userUsername) {
      return undefined;
    }

    return [
      '/',
      accessToken.userUsername,
      'access-tokens',
      accessToken.id.toString()
    ];
  }
}
