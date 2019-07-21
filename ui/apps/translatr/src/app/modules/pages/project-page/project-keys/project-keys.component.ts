import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { filter, take, tap } from 'rxjs/operators';
import { Key, Project } from '@dev/translatr-model';
import { MatDialog, MatSnackBar } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { openKeyEditDialog } from '../../../shared/key-edit-dialog/key-edit-dialog.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-keys',
  templateUrl: './project-keys.component.html',
  styleUrls: ['./project-keys.component.scss']
})
export class ProjectKeysComponent {
  project$ = this.facade.project$;
  keys$ = this.facade.keys$;

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
        this.facade.loadKeys(project.id, { limit: `${limit}` })
      );
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

  openKeyCreationDialog(project: Project): void {
    openKeyEditDialog(this.dialog, { projectId: project.id })
      .afterClosed()
      .pipe(
        take(1),
        filter(key => !!key)
      )
      .subscribe((key => this.router
        .navigate([key.name], { relativeTo: this.route })));
  }
}
