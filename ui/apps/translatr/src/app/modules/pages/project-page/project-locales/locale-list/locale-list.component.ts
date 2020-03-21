import { Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { FileType, Locale, LocaleCriteria, PagedList, Project } from '@dev/translatr-model';
import { openLocaleEditDialog } from '../../../../shared/locale-edit-dialog/locale-edit-dialog.component';
import { filter, take } from 'rxjs/operators';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { trackByFn } from '@translatr/utils';
import { FilterCriteria } from '../../../../shared/list-header/list-header.component';

@Component({
  selector: 'app-locale-list',
  templateUrl: './locale-list.component.html',
  styleUrls: ['./locale-list.component.scss']
})
export class LocaleListComponent {
  @Input() project: Project;
  @Input() locales: PagedList<Locale>;
  @Input() criteria: LocaleCriteria | undefined;
  @Input() search: string;
  @Input() canCreate = false;
  @Input() canDelete = false;

  @Output() fetch = new EventEmitter<LocaleCriteria>();
  @Output() edit = new EventEmitter<Locale>();
  @Output() delete = new EventEmitter<Locale>();
  trackByFn = trackByFn;
  // @ts-ignore
  @HostBinding('style.display') private readonly display = 'block';
  readonly fileTypes: Array<{ type: string; name: string; }> = [
    { type: FileType.JavaProperties, name: 'Java Properties' },
    { type: FileType.PlayMessages, name: 'Play Messages' },
    { type: FileType.Gettext, name: 'Gettext' },
    { type: FileType.Json, name: 'JSON' }
  ];

  constructor(
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
  }

  onFilter(criteria: FilterCriteria): void {
    this.fetch.emit(criteria);
  }

  onEdit(locale: Locale, event: MouseEvent) {
    this.openLocaleDialog(locale);
    this.edit.emit(locale);
    event.stopPropagation();
    event.preventDefault();
    return false;
  }

  onDelete(locale: Locale): void {
    this.delete.emit(locale);
  }

  openLocaleDialog(locale: Partial<Locale>): void {
    openLocaleEditDialog(this.dialog, { ...locale })
      .afterClosed()
      .pipe(
        take(1),
        filter(l => !!l && locale.id === undefined)
      )
      .subscribe((l => this.router
        .navigate([l.name], { relativeTo: this.route })));
  }
}
