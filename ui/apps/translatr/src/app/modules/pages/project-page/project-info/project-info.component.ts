import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Key, Locale, Message, Project } from '@dev/translatr-model';
import { ProjectFacade } from '../+state/project.facade';
import { filter, map, pluck, take } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { openLocaleEditDialog } from '../../../shared/locale-edit-dialog/locale-edit-dialog.component';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { openKeyEditDialog } from '../../../shared/key-edit-dialog/key-edit-dialog.component';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-project-info',
  templateUrl: './project-info.component.html',
  styleUrls: ['./project-info.component.scss']
})
export class ProjectInfoComponent {
  project$ = this.facade.project$.pipe(filter(x => !!x));
  locales$ = this.facade.locales$;
  latestLocales$ = this.locales$.pipe(
    filter(pagedList => !!pagedList && !!pagedList.list),
    pluck('list'),
    map((locales: Locale[]) => {
      return locales
        .slice()
        .sort(
          (a: Locale, b: Locale) =>
            b.whenUpdated.getTime() - a.whenUpdated.getTime()
        )
        .slice(0, 3);
    })
  );
  keys$ = this.facade.keys$;
  latestKeys$: Observable<Key[]> = this.keys$.pipe(
    filter(pagedList => !!pagedList && !!pagedList.list),
    pluck('list'),
    map((keys: Key[]) => {
      console.log('keys', keys);
      return keys
        .slice()
        .sort(
          (a: Key, b: Key) => b.whenUpdated.getTime() - a.whenUpdated.getTime()
        )
        .slice(0, 3);
    })
  );
  latestMessages$: Observable<Message[]> = this.facade.messages$.pipe(
    filter(pagedList => !!pagedList && !!pagedList.list),
    pluck('list'),
    map((messages: Message[]) => {
      console.log('messages', messages);
      return messages.slice(0, 3);
    })
  );

  constructor(
    private readonly facade: ProjectFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
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
