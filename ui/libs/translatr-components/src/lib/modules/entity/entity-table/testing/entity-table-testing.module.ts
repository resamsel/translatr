import { Component, EventEmitter, Input, NgModule, Output, ChangeDetectionStrategy } from '@angular/core';
import { Entity, FilterFieldFilter } from '@dev/translatr-components';
import { PagedList, RequestCriteria } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'entity-table',
  changeDetection: ChangeDetectionStrategy.Eager,
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
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
class MockSelectionActionsComponent {}

@NgModule({
  declarations: [MockEntityTableComponent, MockSelectionActionsComponent],
  exports: [MockEntityTableComponent, MockSelectionActionsComponent]
})
export class EntityTableTestingModule {}
