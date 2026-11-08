import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { Entity, FilterFieldFilter } from '@dev/translatr-components';
import { PagedList, RequestCriteria } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'entity-table',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockEntityTableComponent {
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
  standalone: true,
  selector: 'selection-actions',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockSelectionActionsComponent {}
