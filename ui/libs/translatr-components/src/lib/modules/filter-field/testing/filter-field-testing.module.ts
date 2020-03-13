import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { FilterFieldFilter } from '@dev/translatr-components';
import { MatFormFieldAppearance, ThemePalette } from '@angular/material';

@Component({
  selector: 'dev-filter-field',
  template: ''
})
class MockFilterFieldComponent {
  @Input() filters: ReadonlyArray<FilterFieldFilter>;
  @Input() appearance: MatFormFieldAppearance | 'elevate' = 'standard';
  @Input() color: ThemePalette = 'primary';
  @Input() selection: ReadonlyArray<FilterFieldFilter>;

  @Output() selected = new EventEmitter<ReadonlyArray<FilterFieldFilter>>();
}

@NgModule({
  declarations: [MockFilterFieldComponent],
  exports: [MockFilterFieldComponent]
})
export class FilterFieldTestingModule {
}
