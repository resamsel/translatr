import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { FilterFieldFilter } from '@dev/translatr-components';
import { MatFormFieldAppearance } from '@angular/material';

@Component({
  selector: 'dev-search-field-page',
  templateUrl: './search-field-page.component.html',
  styleUrls: ['./search-field-page.component.css']
})
export class SearchFieldPageComponent implements OnInit {

  searchControl = new FormControl();
  readonly filters: Array<FilterFieldFilter> = [
    {
      title: 'Search',
      key: 'search',
      type: 'string'
    },
    {
      title: 'Missing translation',
      key: 'missing',
      type: 'boolean',
      value: true
    }
  ];
  readonly selection: Array<FilterFieldFilter> = [
    {
      key: 'search',
      type: 'string',
      value: 'foobar'
    }
  ];
  appearances: Array<MatFormFieldAppearance | 'elevate'> = ['elevate'];

  constructor() {
  }

  ngOnInit() {
  }

}
