import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  HostBinding,
  Input,
  OnInit,
  Output,
  Renderer2,
  ViewChild
} from '@angular/core';
import { FilterFieldFilter } from '@translatr/translatr-components/src';
import { FormControl } from '@angular/forms';
import {
  MatAutocompleteTrigger,
  MatCheckboxChange,
  MatFormFieldAppearance,
  MatOptionSelectionChange,
  ThemePalette
} from '@angular/material';
import { isArray } from 'util';

const lowerCaseIncludes = (s: string, search: string): boolean =>
  s.toLowerCase().includes(search.toLowerCase());

const valueOf = (option: FilterFieldFilter, definition: FilterFieldFilter): string | number | boolean => {
  switch (definition.type) {
    case 'option':
    case 'boolean':
      return definition.value;
    default:
      return option.value;
  }
};

const filterOption = (option: FilterFieldFilter, definition: FilterFieldFilter): boolean => {
  switch (definition.type) {
    case 'number':
      return typeof option.value === 'number' && !isNaN(option.value);
    case 'option':
      return option.key === definition.key && option.value === definition.value;
    default:
      return true;
  }
};

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-filter-field',
  templateUrl: './filter-field.component.html',
  styleUrls: ['./filter-field.component.scss']
})
export class FilterFieldComponent implements OnInit {
  @Input() filters: ReadonlyArray<FilterFieldFilter>;
  @Input() appearance: MatFormFieldAppearance | 'elevate' = 'standard';
  @Input() color: ThemePalette = 'primary';
  @Output() selected = new EventEmitter<ReadonlyArray<FilterFieldFilter>>();
  @ViewChild('autocompleteInput', { static: false }) autocompleteInput: ElementRef;
  @ViewChild(MatAutocompleteTrigger, { static: false }) autocompleteTrigger: MatAutocompleteTrigger;

  // @ts-ignore
  @HostBinding('class') private readonly clazz = 'filter-field';

  filterControl = new FormControl('');
  autocompleteOptions: Array<FilterFieldFilter> = [];

  private _selection: ReadonlyArray<FilterFieldFilter>;

  get selection(): ReadonlyArray<FilterFieldFilter> {
    return this._selection;
  }

  @Input() set selection(selection: ReadonlyArray<FilterFieldFilter>) {
    this._selection = selection;
    console.log('selection', selection);
    this._options = isArray(selection) ? selection : [];
  }

  private _options: ReadonlyArray<FilterFieldFilter> = [];

  get options(): ReadonlyArray<FilterFieldFilter> {
    return this._options;
  }

  set options(options: ReadonlyArray<FilterFieldFilter>) {
    this._options = options;
    this.selected.emit(options);
  }

  constructor(public readonly renderer: Renderer2) {
  }

  ngOnInit() {
    this.filterControl.valueChanges
      .subscribe((value: string | FilterFieldFilter) => {
        if (typeof value === 'string') {
          this.updateAutocompleteOptions({
            ...this.filters.find(f => f.key === 'search'),
            key: 'search',
            value: value.trim()
          });
        } else {
          this.updateAutocompleteOptions(value);
        }
      });
    this.updateAutocompleteOptions();
  }

  displayFn(option) {
    return option ? option.title : option;
  }

  onRemoved(key: string) {
    this.removeOption(key);
    if (key === 'search') {
      this.filterControl.setValue('');
    }
  }

  onAutocompleteSelected(selection: MatOptionSelectionChange) {
    this.autocompleteInput.nativeElement.focus();
    this.autocompleteInput.nativeElement.value = '';
    this.filterControl.setValue('');
    this.updateOption(selection.source.value);
  }

  private updateOption(option: FilterFieldFilter): void {
    this.options = [
      ...this.options.filter(o => o.key !== option.key),
      option
    ];
    this.updateAutocompleteOptions();
  }

  private removeOption(key: string): void {
    this.options = this.options.filter(o => o.key !== key);
    this.updateAutocompleteOptions();
  }

  onSelected(filter: FilterFieldFilter, change: MatCheckboxChange) {
    if (change.checked) {
      this.updateOption(filter);
    } else {
      this.removeOption(filter.key);
    }
  }

  private updateAutocompleteOptions(option?: FilterFieldFilter): void {
    const booleans = this.options
      .filter(s => s.type === 'boolean')
      .map(s => s.key);
    const filters = this.filters
      .filter(f => !booleans.includes(f.key));

    if (option !== undefined) {
      this.autocompleteOptions = filters
        .filter(o =>
          (option.value !== '' && !o.allowEmpty)
          || (option.value === '' && o.allowEmpty)
          || (option.value !== '' && lowerCaseIncludes(o.title, option.value.toString())))
        .filter(o => filterOption(o, option))
        .map((o) => ({
            ...o,
          value: valueOf(option, o)
          })
        );
    } else {
      this.autocompleteOptions = filters
        .filter(o => o.allowEmpty);
    }
  }
}
