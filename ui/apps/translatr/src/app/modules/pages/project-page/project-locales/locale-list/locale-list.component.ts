import { Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { KeyCriteria, Locale, LocaleCriteria, PagedList, Project } from '@dev/translatr-model';
import { openLocaleEditDialog } from '../../../../shared/locale-edit-dialog/locale-edit-dialog.component';
import { filter, take } from 'rxjs/operators';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { trackByFn } from '@translatr/utils';

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

  @Output() load = new EventEmitter<KeyCriteria>();
  @Output() more = new EventEmitter<number>();
  @Output() edit = new EventEmitter<Locale>();
  @Output() delete = new EventEmitter<Locale>();
  trackByFn = trackByFn;
  // @ts-ignore
  @HostBinding('style.display') private readonly display = 'block';

  constructor(
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
  }

  onFilter(search: string): void {
    this.load.emit({ search });
  }

  onEdit(locale: Locale, event: MouseEvent) {
    this.edit.emit(locale);
    event.stopPropagation();
    event.preventDefault();
    return false;
  }

  onDelete(locale: Locale, event: MouseEvent) {
    this.delete.emit(locale);
    event.stopPropagation();
    event.preventDefault();
    return false;
  }

  openLocaleCreationDialog(project: Project): void {
    openLocaleEditDialog(this.dialog, { projectId: project.id })
      .afterClosed()
      .pipe(
        take(1),
        filter(locale => !!locale)
      )
      .subscribe((locale => this.router
        .navigate([locale.name], { relativeTo: this.route })));
  }
}
