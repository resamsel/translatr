import { CommonModule } from '@angular/common';
import { Component, EventEmitter, HostBinding, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import {
  ConfirmButtonComponent,
  EmptyViewComponent,
  EmptyViewHeaderComponent,
  EmptyViewContentComponent,
  EmptyViewActionsComponent
} from '@dev/translatr-components';
import { Key, KeyCriteria, PagedList, Project } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { trackByFn } from '@translatr/utils';
import { filter, take } from 'rxjs/operators';
import { NavListComponent } from '../../../../shared/nav-list/nav-list.component';
import { openKeyEditDialog } from '../../../../shared/key-edit-dialog/key-edit-dialog.component';
import { FilterCriteria } from '../../../../shared/list-header/list-header.component';

@Component({
  standalone: true,
  selector: 'app-key-list',
  templateUrl: './key-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./key-list.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    TranslocoModule,
    NavListComponent,
    ConfirmButtonComponent,
    EmptyViewComponent,
    EmptyViewHeaderComponent,
    EmptyViewContentComponent,
    EmptyViewActionsComponent,
    MatListModule,
    MatIconModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatButtonModule
  ]
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
