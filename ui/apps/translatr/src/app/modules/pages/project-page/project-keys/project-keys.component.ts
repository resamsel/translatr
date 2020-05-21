import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { filter, skip, take, takeUntil } from 'rxjs/operators';
import { Key, KeyCriteria, Project } from '@dev/translatr-model';
import { MatSnackBar } from '@angular/material';
import { Router } from '@angular/router';
import { navigate } from '@translatr/utils';
import { combineLatest } from 'rxjs';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-keys',
  templateUrl: './project-keys.component.html',
  styleUrls: ['./project-keys.component.scss']
})
export class ProjectKeysComponent {
  readonly project$ = this.facade.project$.pipe(filter(x => !!x));
  readonly keys$ = this.facade.keys$;
  readonly criteria$ = this.facade.keysCriteria$;
  readonly canModify$ = this.facade.canModifyKey$;
  private readonly context$ = combineLatest([this.criteria$, this.project$])
    .pipe(takeUntil(this.facade.unload$));

  constructor(
    private readonly facade: ProjectFacade,
    private readonly snackBar: MatSnackBar,
    private readonly router: Router
  ) {
    this.context$.subscribe(([criteria, project]: [KeyCriteria, Project]) =>
      this.facade.loadKeys(project.id, criteria)
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
      .subscribe(([k]) =>
        this.snackBar.open(
          `Key ${k.name} deleted`,
          'Dismiss',
          {duration: 5000}
        )
      );
  }
}
