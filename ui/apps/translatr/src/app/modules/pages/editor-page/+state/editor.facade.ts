import { Injectable } from '@angular/core';

import { select, Store } from '@ngrx/store';

import { EditorPartialState } from './editor.reducer';
import { editorQuery } from './editor.selectors';
import {
  LoadKey,
  LoadKeysBy,
  LoadKeySearch,
  LoadLocale, LoadLocalesBy,
  LoadLocaleSearch,
  SaveMessage,
  SelectKey,
  SelectLocale, UnloadEditor
} from './editor.actions';
import { Observable, Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import { Locale } from "../../../../../../../../libs/translatr-model/src/lib/model/locale";
import { PagedList } from "../../../../../../../../libs/translatr-model/src/lib/model/paged-list";
import { Message } from "../../../../../../../../libs/translatr-model/src/lib/model/message";
import { Key } from "../../../../../../../../libs/translatr-model/src/lib/model/key";
import {RequestCriteria} from "../../../../../../../../libs/translatr-model/src/lib/model/request-criteria";

@Injectable()
export class EditorFacade {
  unloadEditor$ = new Subject<void>();

  get locale$(): Observable<Locale> {
    return this.store.pipe(takeUntil(this.unloadEditor$), select(editorQuery.getLocale));
  }

  get locales$(): Observable<PagedList<Locale>> {
    return this.store.pipe(takeUntil(this.unloadEditor$), select(editorQuery.getLocales));
  }

  get localesLoading$(): Observable<boolean> {
    return this.store.pipe(takeUntil(this.unloadEditor$), select(editorQuery.getLocalesLoading));
  }

  get key$(): Observable<Key> {
    return this.store.pipe(takeUntil(this.unloadEditor$), select(editorQuery.getKey));
  }

  get keys$(): Observable<PagedList<Key>> {
    return this.store.pipe(takeUntil(this.unloadEditor$), select(editorQuery.getKeys));
  }

  get keysLoading$(): Observable<boolean> {
    return this.store.pipe(takeUntil(this.unloadEditor$), select(editorQuery.getKeysLoading));
  }

  get selectedLocale$(): Observable<string> {
    return this.store.pipe(takeUntil(this.unloadEditor$), select(editorQuery.getSelectedLocale));
  }

  get selectedKey$(): Observable<string> {
    return this.store.pipe(takeUntil(this.unloadEditor$), select(editorQuery.getSelectedKey));
  }

  get selectedMessage$(): Observable<Message> {
    return this.store.pipe(takeUntil(this.unloadEditor$), select(editorQuery.getSelectedMessage));
  }

  get search$(): Observable<RequestCriteria> {
    return this.store.pipe(takeUntil(this.unloadEditor$), select(editorQuery.getSearch));
  }

  constructor(private store: Store<EditorPartialState>) {
  }

  loadLocaleEditor(username: string, projectName: string, localeName: string): void {
    this.store.dispatch(new LoadLocale({username, projectName, localeName}));
  }

  loadKeyEditor(username: string, projectName: string, keyName: string): void {
    this.store.dispatch(new LoadKey({username, projectName, keyName: keyName}));
  }

  unloadEditor(): void {
    this.unloadEditor$.next();
    this.store.dispatch(new UnloadEditor());
  }

  selectKey(key?: string): void {
    console.log(`Dispatching SelectKey(${key})`);
    this.store.dispatch(new SelectKey({key}));
  }

  selectLocale(locale?: string): void {
    this.store.dispatch(new SelectLocale({locale}));
  }

  saveMessage(message: Message): void {
    this.store.dispatch(new SaveMessage(message));
  }

  loadKeysBy(criteria: RequestCriteria) {
    this.store.dispatch(new LoadKeysBy(criteria));
  }

  updateLocaleSearch(criteria: RequestCriteria) {
    this.store.dispatch(new LoadLocaleSearch(criteria))
  }

  updateKeySearch(criteria: RequestCriteria) {
    this.store.dispatch(new LoadKeySearch(criteria))
  }
}
