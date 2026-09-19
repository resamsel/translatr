import { CommonModule } from '@angular/common';
import { Component, EventEmitter, HostBinding, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
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
import {
  fileTypeNames,
  fileTypes,
  Locale,
  LocaleCriteria,
  PagedList,
  Project
} from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { trackByFn } from '@translatr/utils';
import { filter, take } from 'rxjs/operators';
import { NavListComponent } from '../../../../shared/nav-list/nav-list.component';
import { FilterCriteria } from '../../../../shared/list-header/list-header.component';
import { openLocaleEditDialog } from '../../../../shared/locale-edit-dialog/locale-edit-dialog.component';

@Component({
  standalone: true,
  selector: 'app-locale-list',
  templateUrl: './locale-list.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./locale-list.component.scss'],
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
    MatButtonModule,
    MatMenuModule
  ]
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
  @HostBinding('style.display') protected readonly display = 'block';
  readonly fileTypes: { type: string; name: string }[] = fileTypes.map(fileType => ({
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
        filter(l => !!l && locale.id === undefined)
      )
      .subscribe(l => this.router.navigate([l.name], { relativeTo: this.route }));
  }
}
