import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { take } from 'rxjs/operators';
import { Key, KeyCriteria, Project } from '@dev/translatr-model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-keys',
  templateUrl: './project-keys.component.html',
  styleUrls: ['./project-keys.component.scss']
})
export class ProjectKeysComponent {
  project$ = this.facade.project$;
  keys$ = this.facade.keys$;
  criteria$ = this.facade.keysCriteria$;
  canCreate$ = this.facade.canCreateKey$;

  constructor(
    private readonly facade: ProjectFacade,
    private readonly snackBar: MatSnackBar
  ) {
  }

  onMore(limit: number) {
    this.onLoad({ limit });
  }

  onLoad(criteria: KeyCriteria) {
    this.project$
      .pipe(take(1))
      .subscribe((project: Project) =>
        this.facade.loadKeys(project.id, criteria));
  }

  onEdit(key: Key) {
    this.snackBar.open(
      `Edit key ${key.name}`,
      'Dismiss',
      { duration: 2000 }
    );
  }

  onDelete(key: Key) {
    this.snackBar.open(
      `Delete key ${key.name}`,
      'Undo',
      { duration: 2000 }
    );
  }
}
