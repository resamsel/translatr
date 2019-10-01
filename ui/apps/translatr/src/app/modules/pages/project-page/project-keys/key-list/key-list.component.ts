import { Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { Key, KeyCriteria, PagedList, Project } from '@dev/translatr-model';
import { openKeyEditDialog } from '../../../../shared/key-edit-dialog/key-edit-dialog.component';
import { filter, take } from 'rxjs/operators';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { trackByFn } from '@translatr/utils';

@Component({
  selector: 'app-key-list',
  templateUrl: './key-list.component.html',
  styleUrls: ['./key-list.component.scss']
})
export class KeyListComponent {
  @Input() project: Project;
  @Input() keys: PagedList<Key>;
  @Input() criteria: KeyCriteria | undefined;
  @Output() load = new EventEmitter<KeyCriteria>();
  @Output() more = new EventEmitter<number>();
  @Output() edit = new EventEmitter<Key>();
  @Output() delete = new EventEmitter<Key>();
  trackByFn = trackByFn;
  // @ts-ignore
  @HostBinding('style.display') private readonly display = 'block';

  constructor(
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
  }

  onLoad(criteria: KeyCriteria): void {
    this.load.emit(criteria);
  }

  onFilter(search: string): void {
    this.load.emit({ search });
  }

  onEdit(key: Key, event: MouseEvent): boolean {
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
