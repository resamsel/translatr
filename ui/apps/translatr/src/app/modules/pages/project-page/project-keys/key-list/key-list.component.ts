import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Project } from "../../../../../../../../../libs/translatr-sdk/src/lib/shared/project";
import { Key } from "../../../../../../../../../libs/translatr-sdk/src/lib/shared/key";
import { PagedList } from "../../../../../../../../../libs/translatr-sdk/src/lib/shared/paged-list";

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
