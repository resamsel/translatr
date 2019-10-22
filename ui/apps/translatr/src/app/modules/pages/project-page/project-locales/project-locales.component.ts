import { Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { skip, take } from 'rxjs/operators';
import { Locale, LocaleCriteria, Project } from '@dev/translatr-model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-project-locales',
  templateUrl: './project-locales.component.html',
  styleUrls: ['./project-locales.component.scss']
})
export class ProjectLocalesComponent {
  project$ = this.facade.project$;
  locales$ = this.facade.locales$;
  criteria$ = this.facade.localesCriteria$;
  canModify$ = this.facade.canModifyLocale$;

  constructor(
    private readonly facade: ProjectFacade,
    private readonly snackBar: MatSnackBar
  ) {
  }

  onFetch(criteria: LocaleCriteria) {
    this.project$
      .pipe(take(1))
      .subscribe((project: Project) =>
        this.facade.loadLocales(project.id, criteria));
  }

  onMore(limit: number) {
    this.project$
      .pipe(take(1))
      .subscribe((project: Project) =>
        // FIXME: may interfere with loaded locales from entry point (page)
        this.facade.loadLocales(project.id, { limit })
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
    this.facade.deleteLocale(locale.id);
    this.facade.localeDeleted$
      .pipe(skip(1), take(1))
      .subscribe((l) =>
        this.snackBar.open(
          `Language ${l.displayName} deleted`,
          'Dismiss',
          { duration: 5000 }
        )
      );
  }
}
