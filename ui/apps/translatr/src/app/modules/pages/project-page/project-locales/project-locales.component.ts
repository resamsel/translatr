import { Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { pluck, take } from 'rxjs/operators';
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
  search$ = this.facade.localesCriteria$.pipe(
    pluck('search')
  );

  constructor(
    private readonly facade: ProjectFacade,
    private readonly snackBar: MatSnackBar
  ) {
  }

  onLoad(criteria: LocaleCriteria) {
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
}
