import { Component, OnDestroy, OnInit } from '@angular/core';
import { Message, PagedList } from '@dev/translatr-model';
import { EditorFacade } from './+state/editor.facade';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { distinctUntilChanged, filter, map, takeUntil } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { trackByFn } from '@translatr/utils';
import { MessageItem } from './message-item';
import { FilterFieldFilter, handleFilterFieldSelection } from '@dev/translatr-components';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-key-editor-page',
  templateUrl: './key-editor-page.component.html',
  styleUrls: ['./key-editor-page.component.scss']
})
export class KeyEditorPageComponent implements OnInit, OnDestroy {
  readonly me$ = this.appFacade.me$.pipe(takeUntil(this.facade.unloadEditor$));
  readonly key$ = this.facade.key$;
  readonly messageItems$ = this.facade.keyEditorMessageItems$;
  readonly selectedLocaleName$ = this.facade.selectedLocaleName$;
  readonly selectedMessage$ = this.facade.keySelectedMessage$
    .pipe(map((message: Message | undefined) =>
      message !== undefined ? { ...message } : undefined));
  readonly search$ = this.facade.search$;
  readonly filters: ReadonlyArray<FilterFieldFilter> = [
    {
      key: 'search',
      title: 'Search',
      type: 'string',
      value: ''
    },
    {
      key: 'missing',
      title: 'Missing translation',
      type: 'boolean',
      value: true,
      allowEmpty: true
    }
  ];
  readonly selection$: Observable<ReadonlyArray<FilterFieldFilter>> =
    this.appFacade.queryParams$.pipe(
      map((params: Params) => this.filters
        .filter(f => params[f.key] !== undefined && params[f.key] !== '')
        .map(f => ({ ...f, value: params[f.key] }))
      ),
      distinctUntilChanged((a, b) => a.length === b.length)
    );

  readonly backLink = {
    routerLink: ['..']
  };

  readonly trackByFn = trackByFn;

  constructor(
    private readonly appFacade: AppFacade,
    private readonly facade: EditorFacade,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {
  }

  ngOnInit() {
    this.appFacade.routeParams$
      .pipe(
        filter(p =>
          p.username !== undefined
          && p.projectName !== undefined
          && p.keyName !== undefined
        ),
        takeUntil(this.facade.unloadEditor$)
      )
      .subscribe((params: Params) => {
        this.facade.loadKeyEditor(
          params.username,
          params.projectName,
          params.keyName
        );
      });
    // TODO: let locale be selected in effects?
    this.selectedLocaleName$.pipe(filter(x => !!x))
      .subscribe((selectedLocaleName: string) =>
        this.facade.selectLocale(selectedLocaleName));
  }

  ngOnDestroy(): void {
    this.facade.unloadEditor();
  }

  onSelected(selected: ReadonlyArray<FilterFieldFilter>): Promise<boolean> {
    return handleFilterFieldSelection(this.router, this.filters, selected);
  }

  onKeyChange(value: string) {
    this.router.navigate(['..', value], {
      queryParamsHandling: 'preserve',
      relativeTo: this.route
    });
  }

  toMessages(messageItems: PagedList<MessageItem>): Array<Message> {
    if (messageItems === undefined) {
      return [];
    }

    return messageItems.list.filter(i => !!i.message).map(i => i.message);
  }
}
