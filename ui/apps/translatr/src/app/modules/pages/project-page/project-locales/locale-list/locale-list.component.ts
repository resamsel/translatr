import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Locale, PagedList, Project } from '@dev/translatr-model';
import { openLocaleEditDialog } from '../../../../shared/locale-edit-dialog/locale-edit-dialog.component';
import { filter, take } from 'rxjs/operators';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-locale-list',
  templateUrl: './locale-list.component.html',
  styleUrls: ['./locale-list.component.scss']
})
export class LocaleListComponent {
  @Input() project: Project;
  @Input() locales: PagedList<Locale>;
  @Output() more = new EventEmitter<number>();
  @Output() edit = new EventEmitter<Locale>();
  @Output() delete = new EventEmitter<Locale>();

  constructor(
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
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
