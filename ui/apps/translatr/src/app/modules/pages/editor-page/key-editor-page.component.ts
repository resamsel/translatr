import { Component, OnDestroy, OnInit } from '@angular/core';
import { Key, Message, PagedList } from '@dev/translatr-model';
import { EditorFacade } from './+state/editor.facade';
import { ActivatedRoute, ParamMap, Params, Router } from '@angular/router';
import { distinctUntilChanged, filter, map, take, takeUntil, tap } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { trackByFn } from '@translatr/utils';
import { MessageItem } from './message-item';
import { FilterFieldFilter, FilterFieldSelection } from '@dev/translatr-components';
import { combineLatest, Observable } from 'rxjs';

@Component({
  selector: 'app-key-editor-page',
  templateUrl: './key-editor-page.component.html',
  styleUrls: ['./key-editor-page.component.scss']
})
export class KeyEditorPageComponent implements OnInit, OnDestroy {
  readonly me$ = this.appFacade.me$.pipe(takeUntil(this.facade.unloadEditor$));
  readonly key$ = this.facade.key$;
  readonly messageItems$ = this.facade.keyEditorMessageItems$;
  readonly selectedMessage$ = this.facade.keySelectedMessage$
    .pipe(map((message: Message | undefined) =>
      message !== undefined ? { ...message } : undefined));
  readonly selectedLocale$ = this.facade.selectedLocale$;
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
  readonly selection$: Observable<ReadonlyArray<FilterFieldSelection>> =
    this.route.queryParams.pipe(
      map((params: Params) => this.filters
        .filter(f => params[f.key] !== undefined && params[f.key] !== '')
        .map(f => ({ ...f, value: params[f.key] }))
      ),
      tap(console.log),
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
    this.route.paramMap
      .pipe(takeUntil(this.facade.unloadEditor$))
      .subscribe((params: ParamMap) => {
        this.facade.loadKeyEditor(
          params.get('username'),
          params.get('projectName'),
          params.get('keyName')
        );
      });
    combineLatest([
      this.route.queryParams,
      this.key$.pipe(filter(x => !!x))
    ])
      .pipe(
        takeUntil(this.facade.unloadEditor$)
      )
      .subscribe(([params, key]: [Params, Key]) => {
        console.log('queryParams', params);
        this.facade.loadLocales({
          ...params,
          projectId: key.projectId,
          keyId: key.id
        });
        this.facade.selectLocale(params.locale);
      });
  }

  ngOnDestroy(): void {
    this.facade.unloadEditor();
  }

  onSelected(selected: ReadonlyArray<FilterFieldSelection>): void {
    const params: Params = this.filters.map(f => f.key)
      .reduce(
        (agg, key) => {
          const selection = selected.find(s => s.key === key);
          return {
            ...agg,
            [key]: selection ? selection.value : null
          };
        },
        {}
      );

    this.router.navigate([], {
      queryParamsHandling: 'merge',
      queryParams: params
    });
  }

  onLoadMore(limit: number): void {
    this.key$.pipe(take(1))
      .subscribe((key: Key) =>
        this.facade.loadLocales({
          projectId: key.projectId,
          limit: limit + 25
        }));
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
