import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { defaultFilters, FilterCriteria } from '../list-header.component';

@Component({
  standalone: true,
  selector: 'app-list-header',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockListHeaderComponent {
  @Input() filters = defaultFilters;
  @Input() searchVisible = true;
  @Input() searchEnabled = true;
  @Input() addVisible = true;
  @Input() addEnabled = true;
  @Input() addTooltip = 'Create';
  @Input() removeVisible = false;
  @Input() removeEnabled = false;
  @Input() removeTooltip = 'Remove';
  @Input() criteria: FilterCriteria;

  @Output() readonly filter = new EventEmitter<FilterCriteria>();
  @Output() readonly add = new EventEmitter<void>();
  @Output() readonly remove = new EventEmitter<void>();
}
