import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FilterFieldComponent, FilterFieldFilter } from '@dev/translatr-components';
import { RequestCriteria } from '@dev/translatr-model';

export const defaultFilters: FilterFieldFilter[] = [
  {
    key: 'search',
    type: 'string',
    title: 'search',
    value: ''
  }
];

export type FilterCriteria = RequestCriteria &
  Record<string, string | number | boolean | undefined>;

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-list-header',
  templateUrl: './list-header.component.html',
  styleUrls: ['./list-header.component.scss'],
  imports: [CommonModule, MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatTooltipModule, FilterFieldComponent]
})
export class ListHeaderComponent implements OnInit {
  selection: FilterFieldFilter[] = [];

  @Input() filters = defaultFilters;
  @Output() readonly filter = new EventEmitter<FilterCriteria>();

  private _criteria: FilterCriteria;

  @Input() searchVisible = true;
  @Input() searchEnabled = true;
  @Input() addVisible = true;
  @Input() addEnabled = true;
  @Input() addTooltip = 'Create';
  @Input() removeVisible = false;
  @Input() removeEnabled = false;
  @Input() removeTooltip = 'Remove';

  @Output() readonly add = new EventEmitter<void>();
  @Output() readonly remove = new EventEmitter<void>();

  get criteria(): FilterCriteria {
    return this._criteria;
  }

  @Input() set criteria(criteria: FilterCriteria) {
    this._criteria = criteria;

    this.updateCriteria();
  }

  ngOnInit(): void {
    this.updateCriteria();
  }

  onSelected(selected: ReadonlyArray<FilterFieldFilter>): void {
    if (selected === undefined || selected === null) {
      selected = [];
    }

    const selection = selected.reduce(
      (agg, curr) => ({
        ...agg,
        [curr.key]: curr.value
      }),
      {}
    );
    this.filter.emit(
      this.filters.reduce(
        (agg, curr) => ({
          ...agg,
          [curr.key]: selection[curr.key]
        }),
        {}
      )
    );
  }

  private updateCriteria(): void {
    if (this.criteria) {
      this.selection = this.filters
        .filter(filter => this.criteria[filter.key] !== undefined)
        .map(filter => ({
          key: filter.key,
          value: this.criteria[filter.key],
          type: filter.type
        }));
    } else {
      this.selection = [];
    }
  }
}
