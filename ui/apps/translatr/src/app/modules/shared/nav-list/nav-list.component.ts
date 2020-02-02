import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output, TemplateRef } from '@angular/core';
import { PagedList, RequestCriteria } from '@dev/translatr-model';
import { trackByFn } from '@translatr/utils';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-nav-list',
  templateUrl: './nav-list.component.html',
  styleUrls: ['./nav-list.component.scss']
})
export class NavListComponent {
  @Input() pagedList: PagedList<{ id?: string | number }> | undefined;
  @Input() criteria: RequestCriteria | undefined;
  @Input() direction: 'column' | 'row' = 'column';

  @Input() loadingListLength = 5;
  @Input() showLoadingAvatar = true;
  @Input() loadingIcon = 'account_circle';

  @Input() empty: TemplateRef<any>;
  @Input() nothingFound: TemplateRef<any>;

  @Input() showMore = true;
  @Input() showFilter = false;
  @Input() addTooltip: string;
  @Input() canCreate = false;

  @Output() readonly more = new EventEmitter<number>();
  @Output() readonly create = new EventEmitter<void>();
  @Output() readonly filter = new EventEmitter<string>();

  trackByFn = trackByFn;

  get loadingList(): number[] {
    return Array(this.loadingListLength).map(
      (value: number, index: number) => index
    );
  }

  loadMore(): void {
    if (this.pagedList !== undefined) {
      this.more.emit(this.pagedList.limit * 2);
    }
  }
}
