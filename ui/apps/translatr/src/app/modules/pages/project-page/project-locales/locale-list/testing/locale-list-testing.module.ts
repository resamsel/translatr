import { Component, EventEmitter, Input, NgModule, Output } from '@angular/core';
import { Locale, LocaleCriteria, PagedList, Project } from '@dev/translatr-model';

@Component({
  selector: 'app-locale-list',
  template: ''
})
class MockLocaleListComponent {
  @Input() project: Project;
  @Input() locales: PagedList<Locale>;
  @Input() criteria: LocaleCriteria | undefined;
  @Input() search: string;
  @Input() canCreate = false;
  @Input() canDelete = false;

  @Output() fetch = new EventEmitter<LocaleCriteria>();
  @Output() edit = new EventEmitter<Locale>();
  @Output() delete = new EventEmitter<Locale>();
}

@NgModule({
  declarations: [MockLocaleListComponent],
  exports: [MockLocaleListComponent]
})
export class LocaleListTestingModule {
}
