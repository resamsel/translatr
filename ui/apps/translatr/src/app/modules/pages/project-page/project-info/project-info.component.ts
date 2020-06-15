import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import {
  AccessToken,
  Activity,
  Feature,
  FileType,
  fileTypeNames,
  fileTypes,
  Key,
  Locale,
  Message,
  PagedList,
  Project
} from '@dev/translatr-model';
import { slicePagedList, WINDOW } from '@translatr/utils';
import { Observable } from 'rxjs';
import { filter, map, pluck, take } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';
import { openKeyEditDialog } from '../../../shared/key-edit-dialog/key-edit-dialog.component';
import { openLocaleEditDialog } from '../../../shared/locale-edit-dialog/locale-edit-dialog.component';
import { ProjectFacade } from '../../../shared/project-state';

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
      slicePagedList(
        pagedList,
        5,
        (a: Locale, b: Locale) => b.whenUpdated.getTime() - a.whenUpdated.getTime()
      )
    )
  );
  canCreateLocale$ = this.facade.canModifyLocale$;

  keys$ = this.facade.keys$;
  latestKeys$: Observable<PagedList<Key>> = this.keys$.pipe(
    map((pagedList: PagedList<Key> | undefined) =>
      slicePagedList(
        pagedList,
        5,
        (a: Key, b: Key) => b.whenUpdated.getTime() - a.whenUpdated.getTime()
      )
    )
  );
  canCreateKey$ = this.facade.canModifyKey$;

  latestMessages$: Observable<PagedList<Message>> = this.facade.messages$.pipe(
    map((pagedList: PagedList<Message> | undefined) => slicePagedList(pagedList, 5))
  );

  readonly activities$ = this.facade.activities$.pipe(
    map((pagedList: PagedList<Activity> | undefined) => slicePagedList(pagedList, 8))
  );

  readonly members$ = this.facade.members$;

  readonly accessTokens$ = this.facade.accessTokens$.pipe(
    filter(x => !!x),
    pluck<PagedList<AccessToken>, AccessToken[]>('list')
  );

  readonly endpointUrl = endpointFromLocation(this.window.location);
  readonly FileType = FileType;
  fileType = FileType.PlayMessages;
  accessTokenKey = '${TRANSLATR_ACCESS_TOKEN}';

  readonly fileTypes = fileTypes.map(fileType => ({
    type: fileType,
    name: fileTypeNames[fileType]
  }));

  readonly targets = {
    [FileType.PlayMessages]: 'conf/messages.?{locale.name}',
    [FileType.JavaProperties]: 'src/main/resources/messages_?{locale.name}.properties',
    [FileType.Gettext]: 'locale/{locale.name}/LC_MESSAGES/message.po',
    [FileType.Json]: 'assets/i18n/{locale.name}.json'
  };

  readonly Feature = Feature;

  constructor(
    private readonly facade: ProjectFacade,
    readonly appFacade: AppFacade,
    private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    @Inject(WINDOW) private readonly window: Window
  ) {
    appFacade.me$
      .pipe(
        filter(x => !!x),
        take(1)
      )
      .subscribe(me => facade.loadAccessTokens({ userId: me.id }));
  }

  openLocaleCreationDialog(project: Project): void {
    openLocaleEditDialog(this.dialog, { projectId: project.id })
      .afterClosed()
      .pipe(
        take(1),
        filter(locale => !!locale)
      )
      .subscribe(locale => this.router.navigate([locale.name], { relativeTo: this.route }));
  }

  openKeyCreationDialog(project: Project): void {
    openKeyEditDialog(this.dialog, { projectId: project.id })
      .afterClosed()
      .pipe(
        take(1),
        filter(key => !!key)
      )
      .subscribe(key => this.router.navigate([key.name], { relativeTo: this.route }));
  }
}
