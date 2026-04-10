import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { Entity, FilterFieldFilter } from '@dev/translatr-components';
import { PagedList, RequestCriteria } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'entity-table',
  template: ''
})
class MockEntityTableComponent {
  @Input() dataSource: PagedList<Entity>;
  @Input() displayedColumns: string[];
  @Input() load: RequestCriteria;
  @Input() filters: FilterFieldFilter[] = [
    {
      key: 'search',
      type: 'string',
      title: 'search',
      value: ''
    }
  ];

  @Output() readonly criteria = new EventEmitter<RequestCriteria>();
  @Output() readonly selected = new EventEmitter<Entity[]>();
}

@Component({
  standalone: false,
  selector: 'selection-actions',
  template: ''
})
class MockSelectionActionsComponent {}

@NgModule({
  declarations: [MockEntityTableComponent, MockSelectionActionsComponent],
  exports: [MockEntityTableComponent, MockSelectionActionsComponent]
})
export class EntityTableTestingModule {}
