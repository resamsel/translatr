import { Component, OnDestroy, OnInit } from '@angular/core';
import { EditorFacade } from './+state/editor.facade';
import { Params, Router } from '@angular/router';
import { distinctUntilChanged, filter, map, takeUntil } from 'rxjs/operators';
import { Message } from '@dev/translatr-model';
import { AppFacade } from '../../../+state/app.facade';
import { trackByFn } from '@translatr/utils';
import { Observable } from 'rxjs';
import { FilterFieldFilter, FilterFieldSelection } from '@dev/translatr-components';

@Component({
  selector: 'app-locale-editor-page',
  templateUrl: './locale-editor-page.component.html',
  styleUrls: ['./locale-editor-page.component.scss']
})
export class LocaleEditorPageComponent implements OnInit, OnDestroy {
  readonly me$ = this.appFacade.me$.pipe(takeUntil(this.facade.unloadEditor$));
  readonly locale$ = this.facade.locale$;
  readonly locales$ = this.facade.locales$;
  readonly messageItems$ = this.facade.localeEditorMessageItems$;
  readonly selectedKeyName$ = this.facade.selectedKeyName$;
  readonly selectedMessage$ = this.facade.localeSelectedMessage$
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
  readonly selection$: Observable<ReadonlyArray<FilterFieldSelection>> =
    this.appFacade.queryParams$.pipe(
      map((params: Params) => this.filters
        .filter(f => params[f.key] !== undefined && params[f.key] !== '')
        .map(f => ({ ...f, value: params[f.key] }))
      ),
      distinctUntilChanged((a, b) => a.length === b.length)
    );
  readonly messages$ = this.facade.messagesOfKey$;
  readonly message: Message;

  readonly backLink = {
    routerLink: ['..']
  };

  readonly trackByFn = trackByFn;

  constructor(
    private readonly appFacade: AppFacade,
    private readonly facade: EditorFacade,
    private readonly router: Router
  ) {
  }

  ngOnInit() {
    this.appFacade.routeParams$
      .pipe(
        filter(p =>
          p.username !== undefined
          && p.projectName !== undefined
          && p.localeName !== undefined
        ),
        takeUntil(this.facade.unloadEditor$)
      )
      .subscribe((params: Params) => {
        this.facade.loadLocaleEditor(
          params.username,
          params.projectName,
          params.localeName
        );
      });
    // TODO: let key be selected in effects?
    this.selectedKeyName$.pipe(filter(x => !!x))
      .subscribe((selectedKeyName: string) =>
        this.facade.selectKey(selectedKeyName));
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
    this.router.navigate([], {
      queryParamsHandling: 'merge',
      queryParams: { limit }
    });
  }
}
