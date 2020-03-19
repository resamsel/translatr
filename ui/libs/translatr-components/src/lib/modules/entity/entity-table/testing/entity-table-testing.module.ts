import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { PagedList, RequestCriteria } from '@dev/translatr-model';
import { Entity, FilterFieldFilter } from '@dev/translatr-components';

@Component({
  selector: 'entity-table',
  template: ''
})
class MockEntityTableComponent {
  @Input() dataSource: PagedList<Entity>;
  @Input() displayedColumns: string[];
  @Input() load: RequestCriteria;
  @Input() filters: Array<FilterFieldFilter> = [{
    key: 'search',
    type: 'string',
    title: 'Search',
    value: ''
  }];

  @Output() readonly criteria = new EventEmitter<RequestCriteria>();
  @Output() readonly selected = new EventEmitter<Entity[]>();
}

@Component({
  selector: 'selection-actions',
  template: ''
})
class MockSelectionActionsComponent {
}

@NgModule({
  declarations: [MockEntityTableComponent, MockSelectionActionsComponent],
  exports: [MockEntityTableComponent, MockSelectionActionsComponent]
})
export class EntityTableTestingModule {
}
