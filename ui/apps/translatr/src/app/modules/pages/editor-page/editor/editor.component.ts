import { AfterViewChecked, ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, Output, ViewChild } from '@angular/core';
import { Message, User } from '@dev/translatr-model';
import { EditorFacade } from '../+state/editor.facade';
import { MatTabGroup } from '@angular/material/tabs';
import { CodemirrorComponent } from '@ctrl/ngx-codemirror';
import { Link } from '@dev/translatr-components';
import { HotkeysService } from '@ngneat/hotkeys';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditorComponent implements AfterViewChecked, OnDestroy {
  @Input() me: User;
  @Input() ownerName: string;
  @Input() projectName: string;
  @Input() name: string;
  @Input() message: Message;

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

  constructor(
    private readonly facade: EditorFacade,
    private readonly hotkeysService: HotkeysService
  ) {
    this.subscriptions.push(this.hotkeysService.addShortcut({keys: 'control.arrowdown'})
      .subscribe(() => this.nextItem.emit()));
    this.subscriptions.push(this.hotkeysService.addShortcut({keys: 'control.arrowup'})
      .subscribe(() => this.previousItem.emit()));
    this.subscriptions.push(this.hotkeysService.addShortcut({keys: 'control.enter'})
      .subscribe(() => this.onSave()));
  }

  ngAfterViewChecked(): void {
    if (this.editor) {
      this.editor.codeMirror.refresh();
    }
    if (this.tabs) {
      this.tabs.realignInkBar();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  onSave(): void {
    this.facade.saveMessage(this.message);
  }

  onUseMessage(message: Message) {
    this.message.value = message.value;
  }
}
