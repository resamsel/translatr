import { Component, EventEmitter, Input, NgModule, Output, ChangeDetectionStrategy } from '@angular/core';
import { Key, KeyCriteria, PagedList, Project } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'app-key-list',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
class MockKeyListComponent {
  @Input() project: Project;
  @Input() keys: PagedList<Key>;
  @Input() criteria: KeyCriteria | undefined;
  @Input() canCreate = false;
  @Input() canDelete = false;

  @Output() fetch = new EventEmitter<KeyCriteria>();
  @Output() edit = new EventEmitter<Key>();
  @Output() delete = new EventEmitter<Key>();
}

@NgModule({
  declarations: [MockKeyListComponent],
  exports: [MockKeyListComponent]
})
export class KeyListTestingModule {}
