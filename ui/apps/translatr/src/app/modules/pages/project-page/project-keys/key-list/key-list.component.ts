import { Component, EventEmitter, HostBinding, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { Key, KeyCriteria, PagedList, Project } from '@dev/translatr-model';
import { trackByFn } from '@translatr/utils';
import { filter, take } from 'rxjs/operators';
import { openKeyEditDialog } from '../../../../shared/key-edit-dialog/key-edit-dialog.component';
import { FilterCriteria } from '../../../../shared/list-header/list-header.component';

@Component({
  standalone: false,
  selector: 'app-key-list',
  templateUrl: './key-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./key-list.component.scss']
})
export class KeyListComponent {
  @Input() project: Project;
  @Input() keys: PagedList<Key>;
  @Input() criteria: KeyCriteria | undefined;
  @Input() canCreate = false;
  @Input() canDelete = false;

  @Output() fetch = new EventEmitter<KeyCriteria>();
  @Output() edit = new EventEmitter<Key>();
  @Output() delete = new EventEmitter<Key>();
  trackByFn = trackByFn;
  @HostBinding('style.display') protected readonly display = 'block';

  constructor(
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  onFilter(criteria: FilterCriteria): void {
    this.fetch.emit(criteria);
  }

  onEdit(key: Key, event: MouseEvent): boolean {
    this.openKeyEditDialog(key);
    this.edit.emit(key);
    event.stopPropagation();
    event.preventDefault();
    return false;
  }

  onDelete(key: Key): void {
    this.delete.emit(key);
  }

  openKeyEditDialog(key: Partial<Key>): void {
    openKeyEditDialog(this.dialog, key)
      .afterClosed()
      .pipe(
        take(1),
        filter(k => !!k && key.id === undefined)
      )
      .subscribe(k => this.router.navigate([k.name], { relativeTo: this.route }));
  }
}
