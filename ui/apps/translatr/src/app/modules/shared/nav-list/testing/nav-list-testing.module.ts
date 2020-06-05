import { Component, EventEmitter, Input, NgModule, Output, TemplateRef } from '@angular/core';
import { PagedList } from '@dev/translatr-model';
import { defaultFilters, FilterCriteria } from '../../list-header/list-header.component';

@Component({
  selector: 'app-nav-list',
  template: ''
})
class MockNavListComponent {
  @Input() filters = defaultFilters;
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
  @Input() criteria: FilterCriteria | undefined;
  @Input() pagedList: PagedList<{ id?: string | number }> | undefined;

  @Output() readonly filter = new EventEmitter<FilterCriteria>();
  @Output() readonly create = new EventEmitter<void>();
}

@NgModule({
  declarations: [MockNavListComponent],
  exports: [MockNavListComponent]
})
export class NavListTestingModule {}
