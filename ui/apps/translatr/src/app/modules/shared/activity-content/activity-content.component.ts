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

  constructor() {}

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
    if (!this.activity || !this.activity.after) {
      return undefined;
    }

    return JSON.parse(this.activity.after);
  }
}
