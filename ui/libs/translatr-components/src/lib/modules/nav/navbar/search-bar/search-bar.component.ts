import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { RequestCriteria } from '@dev/translatr-model';
import { MatAutocompleteTrigger, MatOptionSelectionChange } from '@angular/material';

type OptionType = keyof RequestCriteria | 'missing';

interface Option {
  type: OptionType;
  value: string | boolean;
}

const defaultAutocompleteOptions: Array<Option> = [
  { type: 'search', value: '' },
  { type: 'missing', value: true },
  { type: 'missing', value: false }
];

@Component({
  selector: 'app-search-bar',
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.scss']
})
export class SearchBarComponent implements OnInit {
  searchControl = new FormControl('');
  @ViewChild('autocompleteInput', { static: false }) autocompleteInput: ElementRef;
  @ViewChild(MatAutocompleteTrigger, { static: false }) autocompleteTrigger: MatAutocompleteTrigger;
  autocompleteOptions: Array<Option> = [];

  @Output() search = new EventEmitter<RequestCriteria>();

  private _options: ReadonlyArray<Option> = [];

  get options(): ReadonlyArray<Option> {
    return this._options;
  }

  set options(options: ReadonlyArray<Option>) {
    this._options = options;

    const criteria: RequestCriteria = {};

    const search = options.find(o => o.type === 'search');
    if (search && typeof search.value === 'string') {
      criteria.search = search.value;
    } else {
      criteria.search = '';
    }

    console.log('emitting', criteria);
    this.search.emit({
      ...this._criteria,
      ...criteria
    });
  }

  private _criteria: RequestCriteria;

  @Input() set criteria(criteria: RequestCriteria) {
    this._criteria = criteria;
    console.log('criteria', criteria);
    if (criteria !== undefined) {
      if (criteria.search && criteria.search.length > 0) {
        this.updateOption({
          type: 'search',
          value: criteria.search
        });
      } else {
        this.removeOption('search');
      }
    }
  }

  ngOnInit(): void {
    this.searchControl.valueChanges.subscribe((value: string) => {
      console.log('valueChange', value);
      if (value && value.length > 0) {
        this.updateAutocompleteOptions(value);
      }
    });
    this.updateAutocompleteOptions('');
  }

  displayFn(option) {
    return option ? option.title : option;
  }

  onEnter(event: Event) {
    const value = this.searchControl.value;
    this.updateOption({ type: 'search', value });
    this.autocompleteInput.nativeElement.focus();
    this.autocompleteInput.nativeElement.value = '';
    this.searchControl.setValue('');
    event.preventDefault();
  }

  onRemoved(option: Option) {
    console.log('removed', option);
    this.removeOption(option.type);
    if (option.type === 'search') {
      this.searchControl.setValue('');
    }
  }

  onAutocompleteSelected(selection: MatOptionSelectionChange) {
    console.log('autocompleteSelected', selection);
    this.updateOption(selection.source.value);
    this.searchControl.setValue('');
  }

  private updateOption(option: Option): void {
    this.options = [
      ...this.options.filter(o => o.type !== option.type),
      option
    ];
  }

  private removeOption(type: OptionType): void {
    this.options = this.options.filter(o => o.type !== type);
  }

  private updateAutocompleteOptions(value: string): void {
    this.autocompleteOptions = defaultAutocompleteOptions
      .filter(o => o.type !== 'search' || !!value)
      .map((o) => {
        if (o.type === 'search') {
          o.value = value;
        }
        return o;
      });
  }
}
