import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Project } from "../../../../../shared/project";
import { Key } from "../../../../../shared/key";
import { PagedList } from "../../../../../shared/paged-list";

@Component({
  selector: 'app-key-list',
  templateUrl: './key-list.component.html',
  styleUrls: ['./key-list.component.scss']
})
export class KeyListComponent {
  @Input() project: Project;
  @Input() keys: PagedList<Key>;
  @Output() more = new EventEmitter<number>();
}
