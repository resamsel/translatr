import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Locale, LocaleCriteria, Project } from '@dev/translatr-model';
import { navigate } from '@translatr/utils';
import { filter, take, takeUntil, withLatestFrom } from 'rxjs/operators';
import { ProjectFacade } from '../../../shared/project-state';

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
      .pipe(withLatestFrom(this.project$.pipe(filter(x => !!x))), takeUntil(this.facade.unload$))
      .subscribe(([criteria, project]: [LocaleCriteria, Project]) =>
        this.facade.loadLocales(project.id, criteria)
      );
  }

  onFetch(criteria: LocaleCriteria) {
    navigate(this.router, criteria);
  }

  onEdit(locale: Locale) {
    this.snackBar.open(`Edit locale ${locale.displayName}`, 'Dismiss', { duration: 2000 });
  }

  onDelete(locale: Locale) {
    this.facade.deleteLocale(locale.id);
    this.facade.localeModified$
      .pipe(take(1))
      .subscribe(([l]) =>
        this.snackBar.open(`Language ${l.displayName} deleted`, 'Dismiss', { duration: 5000 })
      );
  }
}
