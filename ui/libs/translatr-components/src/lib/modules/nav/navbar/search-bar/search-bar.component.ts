import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { RequestCriteria } from '@dev/translatr-model';

@Component({
  selector: 'app-search-bar',
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.scss']
})
export class SearchBarComponent {
  searchControl = new FormControl('');

  @Input() set criteria(criteria: RequestCriteria) {
    let value: RequestCriteria = {
      search: ''
    };
    if (criteria !== undefined) {
      value = {
        ...criteria,
        search: criteria.search || ''
      };
    }

    this.searchControl.setValue(value.search);
  }

  @Output() search = new EventEmitter<RequestCriteria>();

  onSearch(event: Event) {
    this.search.emit({ search: this.searchControl.value });
    event.preventDefault();
  }
}
