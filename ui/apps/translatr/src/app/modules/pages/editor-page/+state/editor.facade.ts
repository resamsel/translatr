import { Injectable } from '@angular/core';
import {
  KeyCriteria,
  LocaleCriteria,
  Message,
  PagedList,
  RequestCriteria,
  Setting
} from '@dev/translatr-model';
import { select, Store } from '@ngrx/store';
import { MessageCriteria } from '@translatr/translatr-model/src/lib/model/message-criteria';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppFacade } from '../../../../+state/app.facade';
import { MessageItem } from '../message-item';
import { SaveBehavior } from '../save-behavior';
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
  UnloadEditor
} from './editor.actions';
import { EditorPartialState } from './editor.reducer';
import { editorQuery } from './editor.selectors';

@Injectable()
export class EditorFacade {
  readonly unloadEditor$ = new Subject<void>();

  locale$ = this.store.pipe(select(editorQuery.getLocale));
  selectedLocaleName$ = this.store.pipe(select(editorQuery.getSelectedLocaleName));

  locales$ = this.store.pipe(select(editorQuery.getLocales));

  localeEditorMessageItems$: Observable<PagedList<MessageItem>> = this.store.pipe(
    select(editorQuery.getLocaleMessageItems)
  );

  keyEditorMessageItems$: Observable<PagedList<MessageItem>> = this.store.pipe(
    select(editorQuery.getKeyMessageItems)
  );

  key$ = this.store.pipe(select(editorQuery.getKey));
  selectedKeyName$ = this.store.pipe(select(editorQuery.getSelectedKeyName));

  localeSelectedMessage$ = this.store.pipe(select(editorQuery.getLocaleSelectedMessage));
  keySelectedMessage$ = this.store.pipe(select(editorQuery.getKeySelectedMessage));

  search$ = this.store.pipe(select(editorQuery.getSearch));

  message$ = this.store.pipe(select(editorQuery.getMessage));

  messagesOfKey$ = this.store.pipe(select(editorQuery.getMessagesOfKey));

  readonly saveBehavior$ = this.appFacade.settings$.pipe(
    map(settings => (settings ?? {})[Setting.SaveBehavior] ?? 'save')
  );

  constructor(
    private readonly store: Store<EditorPartialState>,
    private readonly appFacade: AppFacade
  ) {}

  loadLocaleEditor(username: string, projectName: string, localeName: string): void {
    this.store.dispatch(new LoadLocale({ username, projectName, localeName }));
  }

  loadKeyEditor(username: string, projectName: string, keyName: string): void {
    this.store.dispatch(new LoadKey({ username, projectName, keyName }));
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

  saveMessageLocally(message: Message): void {
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
    this.appFacade.updateSettings({ [Setting.SaveBehavior]: behavior });
  }
}
