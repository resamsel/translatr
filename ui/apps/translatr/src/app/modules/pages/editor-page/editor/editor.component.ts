import { AfterViewChecked, ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, Output, ViewChild } from '@angular/core';
import { Message, User } from '@dev/translatr-model';
import { EditorFacade } from '../+state/editor.facade';
import { MatTabGroup } from '@angular/material/tabs';
import { CodemirrorComponent } from '@ctrl/ngx-codemirror';
import { Link } from '@dev/translatr-components';
import { HotkeysService } from '@ngneat/hotkeys';
import { Subscription } from 'rxjs';
import { SaveBehavior } from '../save-behavior';
import { filter, skip, take } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslocoService } from '@ngneat/transloco';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditorComponent implements AfterViewChecked, OnDestroy {
  private _message: Message;
  private _originalValue: string;
  @Input() me: User;
  @Input() ownerName: string;
  @Input() projectName: string;
  @Input() name: string;

  get message(): Message {
    return this._message;
  }

  @Input() set message(message: Message) {
    this._message = message;
    this._originalValue = message !== undefined ? message.value : undefined;
  }

  @Input() messages: Array<Message>;

  @Input()
  set backLink(backLink: Link) {
    this._backLink = backLink;
  }

  @Output() readonly nextItem = new EventEmitter<void>();
  @Output() readonly previousItem = new EventEmitter<void>();

  @ViewChild('editor', {read: CodemirrorComponent})
  private editor: CodemirrorComponent;
  @ViewChild('tabs', {read: MatTabGroup, static: true}) private tabs: MatTabGroup;

  private readonly subscriptions: Subscription[] = [];
  readonly options = {
    mode: 'xml',
    lineNumbers: true,
    lineWrapping: true,
    styleActiveLine: true,
    htmlMode: true
  };

  _backLink: Link | undefined;
  get backLink(): Link {
    if (this._backLink) {
      if (this._backLink.name) {
        return this._backLink;
      }

      return {
        ...this._backLink,
        name: this.name
      };
    }

    if (!this.ownerName || !this.projectName) {
      return undefined;
    }

    return {
      routerLink: ['/', this.ownerName, this.projectName],
      name: this.projectName
    };
  }

  readonly saveBehavior$ = this.facade.saveBehavior$;
  readonly SaveBehavior = SaveBehavior;

  constructor(
    private readonly facade: EditorFacade,
    private readonly hotkeysService: HotkeysService,
    private readonly snackBar: MatSnackBar,
    private readonly translocoService: TranslocoService
  ) {
    this.subscriptions.push(this.hotkeysService.addShortcut({
      keys: 'control.arrowdown',
      description: 'Go to next item',
      group: 'Navigation'
    })
      .subscribe(() => this.onNextItem()));
    this.subscriptions.push(this.hotkeysService.addShortcut({
      keys: 'control.arrowup',
      description: 'Go to previous item',
      group: 'Navigation'
    })
      .subscribe(() => this.onPreviousItem()));
    this.subscriptions.push(this.hotkeysService.addShortcut({
      keys: 'control.enter',
      description: 'Save translation',
      group: 'Saving'
    })
      .subscribe(() => this.onSave()));
    this.subscriptions.push(this.hotkeysService.addShortcut({
      keys: 'control.shift.enter',
      description: 'Save and go to next item',
      group: 'Saving'
    }).subscribe(() => this.onSave(SaveBehavior.SaveAndNext)));
  }

  ngAfterViewChecked(): void {
    if (this.editor) {
      this.editor.codeMirror.refresh();
      this.editor.registerOnChange(() => this.facade.saveMessageLocally(this.message));
    }
    if (this.tabs) {
      this.tabs.realignInkBar();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  onUnsavedChanges(): void {
    this.snackBar.open(
      this.translocoService.translate('editor.changes.unsaved'),
      this.translocoService.translate('button.dismiss'),
      {duration: 3000}
    );
  }

  onNextItem(check: boolean = true): void {
    if (check && this.message !== undefined) {
      if (this.message.value === this._originalValue) {
        // only navigate away when translation hasn't changed
        this.nextItem.emit();
      } else {
        this.onUnsavedChanges();
      }
    } else {
      this.nextItem.emit();
    }
  }

  onPreviousItem(check: boolean = true): void {
    if (check && this.message !== undefined) {
      if (this.message.value === this._originalValue) {
        // only navigate away when translation hasn't changed
        this.previousItem.emit();
      } else {
        this.onUnsavedChanges();
      }
    } else {
      this.previousItem.emit();
    }
  }

  onSave(behavior?: SaveBehavior): void {
    this.facade.saveMessage(this.message);
    // wait for successfully saving message
    this.facade.message$.pipe(
      skip(1),
      take(1)
    )
      .subscribe(() => {
        if (behavior !== undefined) {
          if (behavior === SaveBehavior.SaveAndNext) {
            this.onNextItem(false);
          }
        } else {
          this.saveBehavior$
            .pipe(
              take(1),
              filter(b => b === SaveBehavior.SaveAndNext)
            )
            .subscribe(() => this.onNextItem(false));
        }
      });
  }

  onUseMessage(message: Message) {
    this._message.value = message.value;
  }

  onSaveBehavior() {
    this.onSave(SaveBehavior.Save);
    this.facade.updateSaveBehavior(SaveBehavior.Save);
  }

  onSaveAndNextBehavior() {
    this.onSave(SaveBehavior.SaveAndNext);
    this.facade.updateSaveBehavior(SaveBehavior.SaveAndNext);
  }
}
