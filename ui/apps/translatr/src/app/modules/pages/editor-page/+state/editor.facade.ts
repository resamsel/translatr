import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { EditorPartialState } from './editor.reducer';
import { editorQuery } from './editor.selectors';
import {
  LoadKey,
  LoadKeys,
  LoadKeySearch,
  LoadLocale,
  LoadLocales,
  LoadLocaleSearch,
  SaveMessage,
  SelectKey,
  SelectLocale,
  UnloadEditor
} from './editor.actions';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { KeyCriteria, LocaleCriteria, Message, PagedList, RequestCriteria } from '@dev/translatr-model';
import { MessageItem } from '../message-item';

@Injectable()
export class EditorFacade {
  readonly unloadEditor$ = new Subject<void>();

  locale$ = this.store.pipe(
    select(editorQuery.getLocale),
    takeUntil(this.unloadEditor$)
  );

  locales$ = this.store.pipe(
    select(editorQuery.getLocales),
    takeUntil(this.unloadEditor$)
  );

  localeEditorMessageItems$: Observable<PagedList<MessageItem>> =
    this.store.pipe(
      select(editorQuery.getLocaleMessageItems),
      takeUntil(this.unloadEditor$)
    );

  keyEditorMessageItems$: Observable<PagedList<MessageItem>> =
    this.store.pipe(
      select(editorQuery.getKeyMessageItems),
      takeUntil(this.unloadEditor$)
    );

  key$ = this.store.pipe(
    select(editorQuery.getKey),
    takeUntil(this.unloadEditor$)
  );

  localeSelectedMessage$ = this.store.pipe(
    select(editorQuery.getLocaleSelectedMessage),
    takeUntil(this.unloadEditor$)
  );
  keySelectedMessage$ = this.store.pipe(
    select(editorQuery.getKeySelectedMessage),
    takeUntil(this.unloadEditor$)
  );

  selectedLocale$ = this.store.pipe(
    select(editorQuery.getSelectedLocaleName),
    takeUntil(this.unloadEditor$)
  );

  search$ = this.store.pipe(
    select(editorQuery.getSearch),
    takeUntil(this.unloadEditor$)
  );

  constructor(private store: Store<EditorPartialState>) {
  }

  loadLocaleEditor(
    username: string,
    projectName: string,
    localeName: string
  ): void {
    this.store.dispatch(
      new LoadLocale({ username, projectName, localeName })
    );
  }

  loadKeyEditor(username: string, projectName: string, keyName: string): void {
    this.store.dispatch(
      new LoadKey({ username, projectName, keyName: keyName })
    );
  }

  unloadEditor(): void {
    this.unloadEditor$.next();
    this.store.dispatch(new UnloadEditor());
  }

  selectKey(key?: string): void {
    this.store.dispatch(new SelectKey({ key }));
  }

  selectLocale(locale?: string): void {
    this.store.dispatch(new SelectLocale({ locale }));
  }

  saveMessage(message: Message): void {
    this.store.dispatch(new SaveMessage(message));
  }

  loadLocales(criteria: LocaleCriteria) {
    this.store.dispatch(new LoadLocales(criteria));
  }

  loadKeys(criteria: KeyCriteria) {
    this.store.dispatch(new LoadKeys(criteria));
  }

  updateLocaleSearch(criteria: RequestCriteria) {
    this.store.dispatch(new LoadLocaleSearch(criteria));
  }

  updateKeySearch(criteria: RequestCriteria) {
    this.store.dispatch(new LoadKeySearch(criteria));
  }
}
