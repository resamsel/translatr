import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Key, PagedList, Project } from '@dev/translatr-model';
import { openKeyEditDialog } from '../../../../shared/key-edit-dialog/key-edit-dialog.component';
import { filter, take } from 'rxjs/operators';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

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

  constructor(
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
  }

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

  openKeyCreationDialog(project: Project): void {
    openKeyEditDialog(this.dialog, { projectId: project.id })
      .afterClosed()
      .pipe(
        take(1),
        filter(key => !!key)
      )
      .subscribe((key => this.router
        .navigate([key.name], { relativeTo: this.route })));
  }
}
