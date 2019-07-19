import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { ProjectFacade } from '../+state/project.facade';
import { filter, take, tap } from 'rxjs/operators';
import { Key, Project } from '@dev/translatr-model';
import { MatDialog, MatSnackBar } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { openKeyCreationDialog } from '../../../shared/key-creation-dialog/key-creation-dialog.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-keys',
  templateUrl: './project-keys.component.html',
  styleUrls: ['./project-keys.component.scss']
})
export class ProjectKeysComponent implements OnInit {
  project$ = this.facade.project$.pipe(
    tap((project: Project) => {
      if (!!project) {
        this.facade.loadKeys(project.id);
      }
    })
  );
  keys$ = this.facade.keys$;

  constructor(
    private readonly facade: ProjectFacade,
    private readonly snackBar: MatSnackBar,
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
  }

  ngOnInit() {
  }

  onMore(limit: number) {
    this.facade.project$
      .pipe(take(1))
      .subscribe((project: Project) =>
        this.facade.loadKeys(project.id, { limit: `${limit}` })
      );
  }

  onEdit(key: Key) {
    this.snackBar.open(`Edit key ${key.name}`, 'Dismiss', { duration: 2000 });
  }

  onDelete(key: Key) {
    this.snackBar.open(`Delete key ${key.name}`, 'Undo', { duration: 2000 });
  }

  openKeyCreationDialog(project: Project): void {
    openKeyCreationDialog(this.dialog, project.id)
      .afterClosed()
      .pipe(
        take(1),
        filter(key => !!key)
      )
      .subscribe((key => this.router
        .navigate([key.name], { relativeTo: this.route })));
  }
}
