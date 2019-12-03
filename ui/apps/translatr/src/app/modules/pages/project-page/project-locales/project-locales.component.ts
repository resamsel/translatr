import { Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { filter, skip, take, takeUntil } from 'rxjs/operators';
import { Locale, LocaleCriteria, Project } from '@dev/translatr-model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { navigate } from '@translatr/utils';
import { Router } from '@angular/router';

@Component({
  selector: 'app-project-locales',
  templateUrl: './project-locales.component.html',
  styleUrls: ['./project-locales.component.scss']
})
export class ProjectLocalesComponent {
  project$ = this.facade.project$.pipe(filter(x => !!x));
  locales$ = this.facade.locales$;
  criteria$ = this.facade.localesCriteria$;
  canModify$ = this.facade.canModifyLocale$;

  constructor(
    private readonly facade: ProjectFacade,
    private readonly snackBar: MatSnackBar,
    private readonly router: Router
  ) {
    this.criteria$
      .pipe(takeUntil(this.facade.unload$))
      .subscribe((criteria: LocaleCriteria) =>
        this.project$
          .pipe(take(1))
          .subscribe((project: Project) =>
            this.facade.loadLocales(project.id, criteria))
      );
  }

  onFetch(criteria: LocaleCriteria) {
    navigate(this.router, criteria);
  }

  onMore(limit: number) {
    navigate(this.router, { limit });
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
    this.facade.localeModified$
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
