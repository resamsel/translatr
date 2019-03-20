import { Injectable } from '@angular/core';

import { select, Store } from '@ngrx/store';

import { EditorPartialState } from './editor.reducer';
import { editorQuery } from './editor.selectors';
import { LoadKeysBy, LoadKeySearch, LoadLocale, SaveMessage, SelectKey } from './editor.actions';
import { Observable, Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import { Locale } from "../../../../shared/locale";
import { PagedList } from "../../../../shared/paged-list";
import { Message } from "../../../../shared/message";
import { Key } from "../../../../shared/key";

@Injectable()
export class EditorFacade {
  unloadLocaleEditor$ = new Subject<void>();

  get locale$(): Observable<Locale> {
    return this.store.pipe(takeUntil(this.unloadLocaleEditor$), select(editorQuery.getLocale));
  }

  get locales$(): Observable<PagedList<Locale>> {
    return this.store.pipe(takeUntil(this.unloadLocaleEditor$), select(editorQuery.getLocales));
  }

  get keys$(): Observable<PagedList<Key>> {
    return this.store.pipe(takeUntil(this.unloadLocaleEditor$), select(editorQuery.getKeys));
  }

  get keysLoading$(): Observable<boolean> {
    return this.store.pipe(takeUntil(this.unloadLocaleEditor$), select(editorQuery.getKeysLoading));
  }

  get selectedKey$(): Observable<string> {
    return this.store.pipe(takeUntil(this.unloadLocaleEditor$), select(editorQuery.getSelectedKey));
  }

  get selectedMessage$(): Observable<Message> {
    return this.store.pipe(takeUntil(this.unloadLocaleEditor$), select(editorQuery.getSelectedMessage));
  }

  get keySearch$(): Observable<RequestCriteria> {
    return this.store.pipe(takeUntil(this.unloadLocaleEditor$), select(editorQuery.getKeySearch));
  }

  constructor(private store: Store<EditorPartialState>) {
  }

  loadLocaleEditor(username: string, projectName: string, localeName: string): void {
    this.store.dispatch(new LoadLocale({username, projectName, localeName}));
  }

  unloadLocaleEditor(): void {
    this.unloadLocaleEditor$.next();
  }

  selectKey(key?: string): void {
    this.store.dispatch(new SelectKey({key}));
  }

  saveMessage(message: Message): void {
    this.store.dispatch(new SaveMessage(message));
  }

  loadKeysBy(criteria: RequestCriteria) {
    this.store.dispatch(new LoadKeysBy(criteria));
  }

  updateKeySearch(criteria: RequestCriteria) {
    this.store.dispatch(new LoadKeySearch(criteria))
  }
}
