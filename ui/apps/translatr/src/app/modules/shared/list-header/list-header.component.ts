import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { FilterFieldFilter } from '@dev/translatr-components';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-list-header',
  templateUrl: './list-header.component.html',
  styleUrls: ['./list-header.component.scss']
})
export class ListHeaderComponent {
  selection: Array<FilterFieldFilter> = [];
  filters = [{
    key: 'search',
    type: 'string',
    title: 'Search',
    value: ''
  }];

  private _search: string;

  get search(): string {
    return this._search;
  }

  @Input() set search(search: string) {
    console.log('list header search', search);
    if (search === undefined || search === null || search.length === 0) {
      if (this.selection.length !== 0) {
        this.selection = [];
      }
    } else if (search !== this._search) {
      this.selection = [{ key: 'search', value: search, type: 'string' }];
    }

    this._search = search;
  }

  @Input() searchEnabled = true;
  @Input() addVisible = true;
  @Input() addEnabled = true;
  @Input() addTooltip = 'Create';
  @Input() removeVisible = false;
  @Input() removeEnabled = false;
  @Input() removeTooltip = 'Remove';

  @Output() readonly add = new EventEmitter<void>();
  @Output() readonly remove = new EventEmitter<void>();
  @Output() readonly filter = new EventEmitter<string>();

  constructor() {
    this.add.subscribe(() => console.log('ListHeader.add'));
  }

  onSelected(selected: ReadonlyArray<FilterFieldFilter>) {
    if (selected.length > 0 && typeof selected[0].value === 'string') {
      this.filter.emit(selected[0].value);
    } else {
      this.filter.emit('');
    }
  }
}
