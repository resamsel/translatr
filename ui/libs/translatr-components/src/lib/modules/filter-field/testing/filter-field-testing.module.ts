import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { ThemePalette } from '@angular/material/core';
import { MatFormFieldAppearance } from '@angular/material/form-field';
import { FilterFieldFilter } from '@dev/translatr-components';

@Component({
  selector: 'dev-filter-field',
  template: ''
})
class MockFilterFieldComponent {
  @Input() enabled: boolean;
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
export class FilterFieldTestingModule {}
