import {Component, OnDestroy, OnInit} from '@angular/core';
import {EditorFacade} from './+state/editor.facade';
import {ActivatedRoute, ParamMap, Params, Router} from '@angular/router';
import {filter, take, takeUntil} from 'rxjs/operators';
import {Key, Locale, Message, PagedList, RequestCriteria} from '@dev/translatr-model';
import {combineLatest} from 'rxjs';
import {AppFacade} from '../../../+state/app.facade';

@Component({
  selector: 'app-locale-editor-page',
  templateUrl: './locale-editor-page.component.html',
  styleUrls: ['./locale-editor-page.component.scss']
})
export class LocaleEditorPageComponent implements OnInit, OnDestroy {

  me$ = this.appFacade.me$;
  locale$ = this.facade.locale$;
  locales$ = this.facade.locales$;
  keys$ = this.facade.keys$;
  selectedMessage$ = this.facade.selectedMessage$;
  selectedKey$ = this.facade.selectedKey$;
  search$ = this.facade.search$;
  message: Message;

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
        this.facade.loadLocaleEditor(
          params.get('username'),
          params.get('projectName'),
          params.get('localeName')
        );
      });
    this.route.queryParams
      .pipe(takeUntil(this.facade.unloadEditor$))
      .subscribe((params: Params) => {
        this.facade.updateKeySearch(params);
        this.facade.selectKey(params.key);
      });
    this.selectedMessage$.subscribe((message: Message) => {
      if (message === undefined) {
        combineLatest([this.locale$, this.facade.keysLoading$])
          .pipe(take(1), filter(([locale, loading]: [Locale, boolean]) => locale !== undefined && !loading))
          .subscribe(() => this.router.navigate([], {queryParamsHandling: 'merge', queryParams: {key: null}}));
      }
    });
  }

  ngOnDestroy(): void {
    this.facade.unloadEditor();
  }

  onSearch(criteria: RequestCriteria) {
    this.router.navigate([], {queryParamsHandling: 'merge', queryParams: criteria});
  }

  onLoadMore(limit: number): void {
    this.facade.loadKeysBy({limit: `${limit + 25}`});
  }

  messagesOfKey(keys?: PagedList<Key>, keyName?: string): Array<Message> {
    if (keys === undefined || keyName === undefined) {
      return [];
    }

    const key = keys.list.find((k: Key) => k.name === keyName);
    if (key === undefined) {
      return [];
    }

    return Object.keys(key.messages).map((k: string) => key.messages[k]);
  }
}
