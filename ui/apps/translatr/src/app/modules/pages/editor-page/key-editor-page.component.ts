import { Component, OnDestroy, OnInit } from '@angular/core';
import { Message, PagedList, RequestCriteria } from '@dev/translatr-model';
import { EditorFacade } from './+state/editor.facade';
import { ActivatedRoute, ParamMap, Params, Router } from '@angular/router';
import { map, takeUntil } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { trackByFn } from '@translatr/utils';
import { MessageItem } from './message-item';

@Component({
  selector: 'app-key-editor-page',
  templateUrl: './key-editor-page.component.html',
  styleUrls: ['./key-editor-page.component.scss']
})
export class KeyEditorPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  key$ = this.facade.key$;
  messageItems$ = this.facade.keyEditorMessageItems$;
  keys$ = this.facade.keys$;
  selectedMessage$ = this.facade.keySelectedMessage$
    .pipe(map((message: Message | undefined) =>
      message !== undefined ? { ...message } : undefined));
  selectedLocale$ = this.facade.selectedLocale$;
  search$ = this.facade.search$;

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
        this.facade.loadKeyEditor(
          params.get('username'),
          params.get('projectName'),
          params.get('keyName')
        );
      });
    this.route.queryParams
      .pipe(takeUntil(this.facade.unloadEditor$))
      .subscribe((params: Params) => {
        this.facade.updateLocaleSearch(params);
        this.facade.selectLocale(params.locale);
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
    // this.facade.loadLocalesBy({limit: `${limit + 25}`});
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
