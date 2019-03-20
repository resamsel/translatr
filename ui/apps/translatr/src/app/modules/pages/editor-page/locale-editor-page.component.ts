import { Component, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { EditorFacade } from "./+state/editor.facade";
import { ActivatedRoute, ParamMap, Params, Router } from "@angular/router";
import { filter, take, takeUntil } from "rxjs/operators";
import { Message } from "../../../shared/message";
import { MatTabGroup } from "@angular/material";
import { combineLatest } from "rxjs";
import { Locale } from "../../../shared/locale";
import { PagedList } from "../../../shared/paged-list";
import { Key } from "../../../shared/key";

@Component({
  selector: 'app-locale-editor-page',
  templateUrl: './locale-editor-page.component.html',
  styleUrls: ['./locale-editor-page.component.scss']
})
export class LocaleEditorPageComponent implements OnInit, OnDestroy {

  locale$ = this.editorFacade.locale$;
  locales$ = this.editorFacade.locales$;
  keys$ = this.editorFacade.keys$;
  selectedMessage$ = this.editorFacade.selectedMessage$;
  selectedKey$ = this.editorFacade.selectedKey$;
  keySearch$ = this.editorFacade.keySearch$;
  message: Message;
  options = {
    mode: 'xml',
    lineNumbers: true,
    lineWrapping: true,
    styleActiveLine: true,
    htmlMode: true
  };

  @ViewChild('tabs', {read: MatTabGroup}) private tabs: MatTabGroup;

  constructor(
    private readonly editorFacade: EditorFacade,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {
  }

  ngOnInit() {
    this.route.paramMap
      .pipe(takeUntil(this.editorFacade.unloadLocaleEditor$))
      .subscribe((params: ParamMap) => {
        this.editorFacade.loadLocaleEditor(
          params.get('username'),
          params.get('projectName'),
          params.get('localeName')
        );
      });
    this.route.queryParams
      .pipe(takeUntil(this.editorFacade.unloadLocaleEditor$))
      .subscribe((params: Params) => {
        this.editorFacade.updateKeySearch(params);
        this.editorFacade.selectKey(params.key);
      });
    this.selectedMessage$.subscribe((message: Message) => {
      console.log('message', message);
      this.message = message;
      this.tabs.realignInkBar();

      if (message === undefined) {
        console.log('removing key from query params, as no message was selected');
        combineLatest(this.locale$, this.editorFacade.keysLoading$)
          .pipe(take(1), filter(([locale, loading]: [Locale, boolean]) => locale !== undefined && !loading))
          .subscribe(() => this.router.navigate([], {queryParamsHandling: 'merge', queryParams: {key: null}}));
      }
    });
  }

  ngOnDestroy(): void {
    this.editorFacade.unloadLocaleEditor();
  }

  @HostListener('keydown.control.enter')
  onSave(): void {
    this.editorFacade.saveMessage(this.message);
  }

  onSearch(criteria: RequestCriteria) {
    this.router.navigate([], {queryParamsHandling: 'merge', queryParams: criteria});
  }

  onLoadMore(limit: number): void {
    this.editorFacade.loadKeysBy({limit: `${limit + 25}`});
  }

  messagesOfKey(keys: PagedList<Key>, keyName: string): Array<Message> {
    const key = keys.list.find((k: Key) => k.name === keyName);
    if (key === undefined) {
      return [];
    }

    return Object.keys(key.messages).map((k: string) => key.messages[k]);
  }
}
