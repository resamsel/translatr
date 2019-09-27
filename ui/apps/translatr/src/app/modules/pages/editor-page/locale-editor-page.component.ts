import { Component, OnDestroy, OnInit } from '@angular/core';
import { EditorFacade } from './+state/editor.facade';
import { ActivatedRoute, ParamMap, Params, Router } from '@angular/router';
import { map, takeUntil } from 'rxjs/operators';
import { Message, PagedList, RequestCriteria } from '@dev/translatr-model';
import { AppFacade } from '../../../+state/app.facade';
import { trackByFn } from '@translatr/utils';
import { MessageItem } from './message-item';

@Component({
  selector: 'app-locale-editor-page',
  templateUrl: './locale-editor-page.component.html',
  styleUrls: ['./locale-editor-page.component.scss']
})
export class LocaleEditorPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  locale$ = this.facade.locale$;
  locales$ = this.facade.locales$;
  messageItems$ = this.facade.localeEditorMessageItems$;
  selectedMessage$ = this.facade.localeSelectedMessage$
    .pipe(map((message: Message | undefined) =>
      message !== undefined ? { ...message } : undefined));
  selectedKey$ = this.facade.selectedKey$;
  search$ = this.facade.search$;
  message: Message;

  backLink = {
    routerLink: ['..']
  };

  trackByFn = trackByFn;

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
  }

  ngOnDestroy(): void {
    this.facade.unloadEditor();
  }

  onSearch(criteria: RequestCriteria) {
    this.router.navigate([], {
      queryParamsHandling: 'merge',
      queryParams: criteria
    });
  }

  onLoadMore(limit: number): void {
    this.facade.loadKeysBy({ limit: limit + 25 });
  }

  toMessages(messageItems: PagedList<MessageItem>): Array<Message> {
    if (messageItems === undefined) {
      return [];
    }

    return messageItems.list.filter(i => !!i.message).map(i => i.message);
  }
}
