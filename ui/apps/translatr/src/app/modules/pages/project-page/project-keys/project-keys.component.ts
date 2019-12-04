import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { filter, skip, take, takeUntil } from 'rxjs/operators';
import { Key, KeyCriteria, Project } from '@dev/translatr-model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { navigate } from '@translatr/utils';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-keys',
  templateUrl: './project-keys.component.html',
  styleUrls: ['./project-keys.component.scss']
})
export class ProjectKeysComponent {
  project$ = this.facade.project$.pipe(filter(x => !!x));
  keys$ = this.facade.keys$;
  criteria$ = this.facade.keysCriteria$;
  canModify$ = this.facade.canModifyKey$;

  constructor(
    private readonly facade: ProjectFacade,
    private readonly snackBar: MatSnackBar,
    private readonly router: Router
  ) {
    this.criteria$
      .pipe(takeUntil(this.facade.unload$))
      .subscribe((criteria: KeyCriteria) =>
        this.project$
          .pipe(take(1))
          .subscribe((project: Project) =>
            this.facade.loadKeys(project.id, criteria))
      );
  }

  onMore(limit: number) {
    navigate(this.router, { limit });
  }

  onFetch(criteria: KeyCriteria) {
    navigate(this.router, criteria);
  }

  onEdit(key: Key) {
    this.snackBar.open(
      `Edit key ${key.name}`,
      'Dismiss',
      { duration: 2000 }
    );
  }

  onDelete(key: Key) {
    this.facade.deleteKey(key.id);
    this.facade.keyModified$
      .pipe(skip(1), take(1))
      .subscribe((k) =>
        this.snackBar.open(
          `Key ${k.name} deleted`,
          'Dismiss',
          { duration: 5000 }
        )
      );
  }
}
