import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { Feature, Key, Locale, Message, PagedList, Project } from '@dev/translatr-model';
import { ProjectFacade } from '../+state/project.facade';
import { filter, map, take } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { openLocaleEditDialog } from '../../../shared/locale-edit-dialog/locale-edit-dialog.component';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { openKeyEditDialog } from '../../../shared/key-edit-dialog/key-edit-dialog.component';
import { slicePagedList, WINDOW } from '@translatr/utils';

function endpointFromLocation(location: Location) {
  return `${location.protocol}//${location.host}`;
}

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
    map((pagedList: PagedList<Locale> | undefined) =>
      slicePagedList(pagedList, 3, (a: Locale, b: Locale) =>
        b.whenUpdated.getTime() - a.whenUpdated.getTime())
    )
  );
  canCreateLocale$ = this.facade.canModifyLocale$;

  keys$ = this.facade.keys$;
  latestKeys$: Observable<PagedList<Key>> = this.keys$.pipe(
    map((pagedList: PagedList<Key> | undefined) =>
      slicePagedList(pagedList, 3, (a: Key, b: Key) =>
        b.whenUpdated.getTime() - a.whenUpdated.getTime())
    )
  );
  canCreateKey$ = this.facade.canModifyKey$;

  latestMessages$: Observable<PagedList<Message>> = this.facade.messages$.pipe(
    map((pagedList: PagedList<Message> | undefined) =>
      slicePagedList(pagedList, 3)
    )
  );

  readonly members$ = this.facade.members$;
  readonly endpointUrl = endpointFromLocation(this.window.location);
  fileType = 'play_messages';

  readonly Feature = Feature;

  constructor(
    private readonly facade: ProjectFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    @Inject(WINDOW) private readonly window: Window
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
