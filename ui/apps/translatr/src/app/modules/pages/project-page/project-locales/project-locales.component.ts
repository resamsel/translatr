import { Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { filter, take } from 'rxjs/operators';
import { Locale, Project } from '@dev/translatr-model';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { openLocaleEditDialog } from '../../../shared/locale-edit-dialog/locale-edit-dialog.component';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-project-locales',
  templateUrl: './project-locales.component.html',
  styleUrls: ['./project-locales.component.scss']
})
export class ProjectLocalesComponent {
  project$ = this.facade.project$;
  locales$ = this.facade.locales$;

  constructor(
    private readonly facade: ProjectFacade,
    private readonly snackBar: MatSnackBar,
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
  }

  onMore(limit: number) {
    this.project$
      .pipe(take(1))
      .subscribe((project: Project) =>
        // FIXME: may interfere with loaded locales from entry point (page)
        this.facade.loadLocales(project.id, { limit: `${limit}` })
      );
  }

  onEdit(locale: Locale) {
    this.snackBar.open(
      `Edit locale ${locale.displayName}`,
      'Dismiss',
      { duration: 2000 }
    );
  }

  onDelete(locale: Locale) {
    this.snackBar.open(
      `Delete locale ${locale.displayName}`,
      'Undo',
      { duration: 2000 }
    );
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
