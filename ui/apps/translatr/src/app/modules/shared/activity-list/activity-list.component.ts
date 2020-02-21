import { ChangeDetectionStrategy, Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { Activity, PagedList } from '@dev/translatr-model';
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
      default:
        return 'vpn_key';
    }
  }
}
