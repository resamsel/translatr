import { Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { fileTypeNames, fileTypes, Locale, LocaleCriteria, PagedList, Project } from '@dev/translatr-model';
import { trackByFn } from '@translatr/utils';
import { filter, take } from 'rxjs/operators';
import { FilterCriteria } from '../../../../shared/list-header/list-header.component';
import { openLocaleEditDialog } from '../../../../shared/locale-edit-dialog/locale-edit-dialog.component';

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
  readonly fileTypes: Array<{ type: string; name: string }> = fileTypes.map((fileType) => ({
    type: fileType,
    name: fileTypeNames[fileType]
  }));

  constructor(
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

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
        filter((l) => !!l && locale.id === undefined)
      )
      .subscribe((l) => this.router.navigate([l.name], { relativeTo: this.route }));
  }
}
