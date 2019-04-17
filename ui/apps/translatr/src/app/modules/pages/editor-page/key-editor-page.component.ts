import {Component, OnDestroy, OnInit} from '@angular/core';
import {Message} from "../../../../../../../libs/translatr-sdk/src/lib/shared/message";
import {EditorFacade} from "./+state/editor.facade";
import {ActivatedRoute, ParamMap, Params, Router} from "@angular/router";
import {filter, take, takeUntil} from "rxjs/operators";
import {combineLatest} from "rxjs";
import {PagedList} from "../../../../../../../libs/translatr-sdk/src/lib/shared/paged-list";
import {Key} from "../../../../../../../libs/translatr-sdk/src/lib/shared/key";
import {Locale} from "../../../../../../../libs/translatr-sdk/src/lib/shared/locale";
import {RequestCriteria} from "../../../../../../../libs/translatr-sdk/src/lib/shared/request-criteria";
import {AppFacade} from "../../../+state/app.facade";

@Component({
  selector: 'app-key-editor-page',
  templateUrl: './key-editor-page.component.html',
  styleUrls: ['./key-editor-page.component.scss']
})
export class KeyEditorPageComponent implements OnInit, OnDestroy {
  me$ = this.appFacade.me$;
  key$ = this.facade.key$;
  locales$ = this.facade.locales$;
  keys$ = this.facade.keys$;
  selectedMessage$ = this.facade.selectedMessage$;
  selectedLocale$ = this.facade.selectedLocale$;
  search$ = this.facade.search$;

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
    this.selectedMessage$.subscribe((message: Message) => {
      if (message === undefined) {
        combineLatest(this.key$, this.facade.localesLoading$)
          .pipe(take(1), filter(([key, loading]: [Key, boolean]) => key !== undefined && !loading))
          .subscribe(() => this.router.navigate([], {queryParamsHandling: 'merge', queryParams: {locale: null}}));
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
    // this.facade.loadLocalesBy({limit: `${limit + 25}`});
  }

  messagesOfLocale(locales?: PagedList<Locale>, localeName?: string): Array<Message> {
    if (locales === undefined || localeName === undefined) {
      return [];
    }

    const locale = locales.list.find((l: Locale) => l.name === localeName);
    if (locale === undefined || locale.messages === undefined) {
      return [];
    }

    return Object.keys(locale.messages).map((k: string) => locale.messages[k]);
  }

  onKeyChange(value: string) {
    this.router.navigate(['..', value], {queryParamsHandling: 'preserve', relativeTo: this.route});
  }
}
