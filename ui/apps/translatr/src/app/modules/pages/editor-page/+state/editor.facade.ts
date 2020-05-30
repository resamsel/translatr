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
  LoadMessagesOfKey,
  SaveMessage,
  SelectKey,
  SelectLocale,
  UnloadEditor,
  UpdateSaveBehavior
} from './editor.actions';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { KeyCriteria, LocaleCriteria, Message, PagedList, RequestCriteria } from '@dev/translatr-model';
import { MessageItem } from '../message-item';
import { MessageCriteria } from '@translatr/translatr-model/src/lib/model/message-criteria';
import { SaveBehavior } from '../save-behavior';

@Injectable()
export class EditorFacade {
  readonly unloadEditor$ = new Subject<void>();

  locale$ = this.store.pipe(
    select(editorQuery.getLocale),
    takeUntil(this.unloadEditor$)
  );
  selectedLocaleName$ = this.store.pipe(
    select(editorQuery.getSelectedLocaleName)
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
  selectedKeyName$ = this.store.pipe(
    select(editorQuery.getSelectedKeyName),
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

  search$ = this.store.pipe(
    select(editorQuery.getSearch),
    takeUntil(this.unloadEditor$)
  );

  message$ = this.store.pipe(select(editorQuery.getMessage));

  messagesOfKey$ = this.store.pipe(
    select(editorQuery.getMessagesOfKey),
    takeUntil(this.unloadEditor$)
  );

  readonly saveBehavior$ = this.store.pipe(select(editorQuery.getSaveBehavior));

  constructor(private readonly store: Store<EditorPartialState>) {
  }

  loadLocaleEditor(
    username: string,
    projectName: string,
    localeName: string
  ): void {
    this.store.dispatch(
      new LoadLocale({username, projectName, localeName})
    );
  }

  loadKeyEditor(username: string, projectName: string, keyName: string): void {
    this.store.dispatch(
      new LoadKey({username, projectName, keyName: keyName})
    );
  }

  unloadEditor(): void {
    this.unloadEditor$.next();
    this.store.dispatch(new UnloadEditor());
  }

  selectKey(key?: string): void {
    this.store.dispatch(new SelectKey({key}));
  }

  selectLocale(locale?: string): void {
    this.store.dispatch(new SelectLocale({locale}));
  }

  saveMessage(message: Message): void {
    this.store.dispatch(new SaveMessage(message));
  }

  saveMessageLocally(message: Message): void {
    console.log('dispatch savemessage');
    this.store.dispatch(new SaveMessage(message, false));
  }

  loadLocales(criteria: LocaleCriteria) {
    this.store.dispatch(new LoadLocales(criteria));
  }

  loadKeys(criteria: KeyCriteria) {
    this.store.dispatch(new LoadKeys(criteria));
  }

  loadMessagesOfKey(criteria: MessageCriteria): void {
    this.store.dispatch(new LoadMessagesOfKey(criteria));
  }

  updateLocaleSearch(criteria: RequestCriteria) {
    this.store.dispatch(new LoadLocaleSearch(criteria));
  }

  updateKeySearch(criteria: RequestCriteria) {
    this.store.dispatch(new LoadKeySearch(criteria));
  }

  updateSaveBehavior(behavior: SaveBehavior) {
    this.store.dispatch(new UpdateSaveBehavior(behavior));
  }
}
