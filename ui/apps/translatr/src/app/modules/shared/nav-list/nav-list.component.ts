import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output, TemplateRef } from '@angular/core';
import { PagedList } from '@dev/translatr-model';
import { trackByFn } from '@translatr/utils';
import { defaultFilters, FilterCriteria } from '../list-header/list-header.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-nav-list',
  templateUrl: './nav-list.component.html',
  styleUrls: ['./nav-list.component.scss']
})
export class NavListComponent {
  @Input() filters = defaultFilters;
  @Output() readonly filter = new EventEmitter<FilterCriteria>();
  queryParams: {};
  searchEnabled = false;

  private _criteria: FilterCriteria | undefined;

  get criteria(): FilterCriteria | undefined {
    return this._criteria;
  }

  @Input() set criteria(criteria: FilterCriteria | undefined) {
    this._criteria = criteria;

    this.updateSearchEnabled();
  }

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

  @Output() readonly create = new EventEmitter<void>();

  private _pagedList: PagedList<{ id?: string | number }> | undefined;

  trackByFn = trackByFn;

  get pagedList(): PagedList<{ id?: string | number }> | undefined {
    return this._pagedList;
  }

  @Input() set pagedList(pagedList: PagedList<{ id?: string | number }> | undefined) {
    this._pagedList = pagedList;

    this.queryParams = pagedList !== undefined ? { limit: pagedList.limit * 2 } : {};

    this.updateSearchEnabled();
  }

  get loadingList(): number[] {
    return Array(this.loadingListLength).map((value: number, index: number) => index);
  }

  onFilter(criteria: FilterCriteria): void {
    this.filter.emit(criteria);
  }

  private updateSearchEnabled(): void {
    this.searchEnabled =
      this.showFilter &&
      (this.pagedList === undefined ||
        this.pagedList.list.length > 0 ||
        (this._criteria !== undefined &&
          this._criteria.search !== undefined &&
          this._criteria.search.length > 0));
  }
}
