import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { Key, KeyCriteria, PagedList, Project } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'app-key-list',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockKeyListComponent {
  @Input() project: Project;
  @Input() keys: PagedList<Key>;
  @Input() criteria: KeyCriteria | undefined;
  @Input() canCreate = false;
  @Input() canDelete = false;

  @Output() fetch = new EventEmitter<KeyCriteria>();
  @Output() edit = new EventEmitter<Key>();
  @Output() delete = new EventEmitter<Key>();
}
