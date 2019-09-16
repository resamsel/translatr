import { Injectable } from '@angular/core';
import { select, Store } from '@ngrx/store';
import { EditorPartialState } from './editor.reducer';
import { editorQuery } from './editor.selectors';
import {
  LoadKey,
  LoadKeysBy,
  LoadKeySearch,
  LoadLocale,
  LoadLocaleSearch,
  SaveMessage,
  SelectKey,
  SelectLocale,
  UnloadEditor
} from './editor.actions';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Key, Locale, Message, PagedList, RequestCriteria } from '@dev/translatr-model';

@Injectable()
export class EditorFacade {
  unloadEditor$ = new Subject<void>();

  get locale$(): Observable<Locale> {
    return this.store.pipe(
      select(editorQuery.getLocale),
      takeUntil(this.unloadEditor$)
    );
  }

  get locales$(): Observable<PagedList<Locale>> {
    return this.store.pipe(
      select(editorQuery.getLocales),
      takeUntil(this.unloadEditor$)
    );
  }

  get localesLoading$(): Observable<boolean> {
    return this.store.pipe(
      select(editorQuery.getLocalesLoading),
      takeUntil(this.unloadEditor$)
    );
  }

  get key$(): Observable<Key> {
    return this.store.pipe(
      select(editorQuery.getKey),
      takeUntil(this.unloadEditor$)
    );
  }

  get keys$(): Observable<PagedList<Key>> {
    return this.store.pipe(
      select(editorQuery.getKeys),
      takeUntil(this.unloadEditor$)
    );
  }

  get keysLoading$(): Observable<boolean> {
    return this.store.pipe(
      select(editorQuery.getKeysLoading),
      takeUntil(this.unloadEditor$)
    );
  }

  get selectedLocale$(): Observable<string> {
    return this.store.pipe(
      select(editorQuery.getSelectedLocale),
      takeUntil(this.unloadEditor$)
    );
  }

  get selectedKey$(): Observable<string> {
    return this.store.pipe(
      select(editorQuery.getSelectedKey),
      takeUntil(this.unloadEditor$)
    );
  }

  get selectedMessage$(): Observable<Message> {
    return this.store.pipe(
      select(editorQuery.getSelectedMessage),
      takeUntil(this.unloadEditor$)
    );
  }

  get search$(): Observable<RequestCriteria> {
    return this.store.pipe(
      select(editorQuery.getSearch),
      takeUntil(this.unloadEditor$)
    );
  }

  constructor(private store: Store<EditorPartialState>) {}

  loadLocaleEditor(
    username: string,
    projectName: string,
    localeName: string
  ): void {
    this.store.dispatch(new LoadLocale({ username, projectName, localeName }));
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

  loadKeysBy(criteria: RequestCriteria) {
    this.store.dispatch(new LoadKeysBy(criteria));
  }

  updateLocaleSearch(criteria: RequestCriteria) {
    this.store.dispatch(new LoadLocaleSearch(criteria));
  }

  updateKeySearch(criteria: RequestCriteria) {
    this.store.dispatch(new LoadKeySearch(criteria));
  }
}
