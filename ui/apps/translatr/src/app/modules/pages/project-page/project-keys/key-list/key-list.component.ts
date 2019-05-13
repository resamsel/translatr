import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Key, PagedList, Project } from '@dev/translatr-model';

@Component({
  selector: 'app-key-list',
  templateUrl: './key-list.component.html',
  styleUrls: ['./key-list.component.scss']
})
export class KeyListComponent {
  @Input() project: Project;
  @Input() keys: PagedList<Key>;
  @Output() more = new EventEmitter<number>();
  @Output() edit = new EventEmitter<Key>();
  @Output() delete = new EventEmitter<Key>();

  onEdit(key: Key, event: MouseEvent) {
    this.edit.emit(key);
    event.stopPropagation();
    event.preventDefault();
    return false;
  }

  onDelete(key: Key, event: MouseEvent) {
    this.delete.emit(key);
    event.stopPropagation();
    event.preventDefault();
    return false;
  }
}
