import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Params, Router } from '@angular/router';
import { FilterFieldFilter, handleFilterFieldSelection } from '@dev/translatr-components';
import { Key, Message } from '@dev/translatr-model';
import { TranslocoService } from '@ngneat/transloco';
import { trackByFn } from '@translatr/utils';
import { combineLatest, Observable } from 'rxjs';
import { distinctUntilChanged, filter, map, take, takeUntil, tap } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { openKeyEditDialog } from '../../shared/key-edit-dialog/key-edit-dialog.component';
import { ProjectFacade } from '../../shared/project-state/+state';
import { EditorFacade } from './+state/editor.facade';
import { navigateItems } from './navigate-utils';

const localeComparator = (a: Params, b: Params): boolean =>
  a.username === b.username && a.projectName === b.projectName && a.localeName === b.localeName;

const filterComparator = (a: Params, b: Params): boolean =>
  a.search === b.search && a.missing === b.missing;

@Component({
  selector: 'app-locale-editor-page',
  templateUrl: './locale-editor-page.component.html',
  styleUrls: ['./locale-editor-page.component.scss']
})
export class LocaleEditorPageComponent implements OnInit, OnDestroy {
  readonly me$ = this.appFacade.me$;
  readonly locale$ = this.facade.locale$;
  readonly locales$ = this.facade.locales$;
  readonly messageItems$ = this.facade.localeEditorMessageItems$;
  readonly selectedKeyName$ = this.facade.selectedKeyName$;
  readonly selectedMessage$ = this.facade.localeSelectedMessage$.pipe(
    map((message: Message | undefined) => (message !== undefined ? { ...message } : undefined))
  );
  readonly search$ = this.facade.search$;
  readonly filters: ReadonlyArray<FilterFieldFilter> = [
    {
      key: 'search',
      title: 'search',
      type: 'string',
      value: ''
    },
    {
      key: 'missing',
      title: 'message.missing',
      type: 'boolean',
      value: true,
      allowEmpty: true
    }
  ];
  readonly selection$: Observable<
    ReadonlyArray<FilterFieldFilter>
  > = this.appFacade.queryParams$.pipe(
    map((params: Params) =>
      this.filters
        .filter(f => params[f.key] !== undefined && params[f.key] !== '')
        .map(f => ({ ...f, value: params[f.key] }))
    ),
    distinctUntilChanged((a, b) => a.length === b.length)
  );
  readonly messages$ = this.facade.messagesOfKey$;
  readonly message: Message;
  readonly params$ = combineLatest([
    this.appFacade.routeParams$.pipe(
      filter(
        p => p.username !== undefined && p.projectName !== undefined && p.localeName !== undefined
      ),
      distinctUntilChanged(localeComparator)
    ),
    this.appFacade.queryParams$.pipe(distinctUntilChanged(filterComparator)),
    this.translocoService.langChanges$.pipe(distinctUntilChanged())
  ]);
  readonly canCreateKey$ = this.projectFacade.canModifyKey$;

  readonly backLink = {
    routerLink: ['..']
  };

  readonly trackByFn = trackByFn;

  constructor(
    private readonly appFacade: AppFacade,
    private readonly facade: EditorFacade,
    private readonly projectFacade: ProjectFacade,
    private readonly translocoService: TranslocoService,
    private readonly router: Router,
    private readonly dialog: MatDialog
  ) {}

  ngOnInit() {
    this.params$
      .pipe(takeUntil(this.facade.unloadEditor$))
      .subscribe(([params, _a, _b]: [Params, Params, string]) => {
        this.facade.loadLocaleEditor(params.username, params.projectName, params.localeName);
      });
    // TODO: let key be selected in effects?
    this.selectedKeyName$
      .pipe(filter(x => !!x))
      .subscribe((selectedKeyName: string) => this.facade.selectKey(selectedKeyName));
  }

  ngOnDestroy(): void {
    this.facade.unloadEditor();
  }

  onSelected(selected: ReadonlyArray<FilterFieldFilter>): Promise<boolean> {
    return handleFilterFieldSelection(this.router, this.filters, selected);
  }

  onNextItem(): void {
    navigateItems(
      this.messageItems$,
      this.selectedKeyName$,
      (messageItem, selected) => messageItem.key.name === selected,
      () => 0,
      index => (index ?? 0) + 1
    )
      .then(messageItem => this.router.navigate([], { queryParams: { key: messageItem.key.name } }))
      .catch();
  }

  onPreviousItem(): void {
    navigateItems(
      this.messageItems$,
      this.selectedKeyName$,
      (messageItem, selected) => messageItem.key.name === selected,
      length => length - 1,
      index => (index ?? 0) - 1
    )
      .then(messageItem => this.router.navigate([], { queryParams: { key: messageItem.key.name } }))
      .catch();
  }

  openKeyDialog(key: Partial<Key>): void {
    openKeyEditDialog(
      this.dialog,
      { ...key },
      k => this.projectFacade.createKey(k),
      k => this.projectFacade.updateKey(k),
      this.projectFacade.keyModified$
    )
      .afterClosed()
      .pipe(
        take(1),
        filter(k => !!k && key.id === undefined),
        tap(k => this.facade.loadKeys({ projectId: k.projectId }))
      )
      .subscribe(k => this.router.navigate([], { queryParams: { key: k.name } }));
  }
}
