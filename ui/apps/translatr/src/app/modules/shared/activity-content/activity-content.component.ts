import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { Activity, Key, Locale, Message, Project } from '@dev/translatr-model';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-activity-content',
  templateUrl: './activity-content.component.html',
  styleUrls: ['./activity-content.component.scss']
})
export class ActivityContentComponent implements OnInit {
  @Input() activity: Activity;

  get contentType(): string {
    if (!this.activity) {
      return undefined;
    }

    return this.activity.contentType.replace('dto.', '').toLowerCase();
  }

  constructor() {
  }

  ngOnInit() {
    console.log('activity', this.activity);
  }

  get project(): Project | undefined {
    return this.parse();
  }

  get locale(): Locale | undefined {
    return this.parse();
  }

  get key(): Key | undefined {
    return this.parse();
  }

  get message(): Message | undefined {
    return this.parse();
  }

  parse<T>(): T | undefined {
    if (!this.activity || (!this.activity.after && !this.activity.before)) {
      return undefined;
    }

    if (this.activity.after) {
      return JSON.parse(this.activity.after);
    }

    // for deleted objects
    return JSON.parse(this.activity.before);
  }

  linkToProject(project: Project): string[] | undefined {
    if (!project || !project.ownerUsername || !project.name) {
      return undefined;
    }

    return [project && project.ownerUsername && project.name];
  }

  linkToProjectOf(e: Locale | Key | Message): string[] | undefined {
    if (!e || !e.projectOwnerUsername || !e.projectName) {
      return undefined;
    }

    return ['/', e.projectOwnerUsername, e.projectName];
  }

  linkToLocale(locale: Locale): string[] | undefined {
    if (!locale || !locale.projectOwnerUsername || !locale.projectName || !locale.name) {
      return undefined;
    }

    return [
      '/',
      locale.projectOwnerUsername,
      locale.projectName,
      'locales',
      locale.name
    ];
  }

  linkToKey(key: Key) {
    if (!key || !key.projectOwnerUsername || !key.projectName || !key.name) {
      return undefined;
    }

    return [
      '/',
      key.projectOwnerUsername,
      key.projectName,
      'keys',
      key.name
    ];
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
}
