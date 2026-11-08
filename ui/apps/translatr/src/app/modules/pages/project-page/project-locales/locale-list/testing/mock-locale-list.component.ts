import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { Locale, LocaleCriteria, PagedList, Project } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'app-locale-list',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockLocaleListComponent {
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
