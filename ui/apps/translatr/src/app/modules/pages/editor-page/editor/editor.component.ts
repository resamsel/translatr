import { AfterViewChecked, ChangeDetectionStrategy, Component, HostListener, Input, ViewChild } from '@angular/core';
import { Message, User } from '@dev/translatr-model';
import { EditorFacade } from '../+state/editor.facade';
import { MatTabGroup } from '@angular/material';
import { CodemirrorComponent } from '@ctrl/ngx-codemirror';
import { Link } from '../../../nav/sidenav/sidenav.component';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditorComponent implements AfterViewChecked {
  @Input() me: User;
  @Input() ownerName: string;
  @Input() projectName: string;
  @Input() name: string;

  @Input() set message(message: Message) {
    this._message = { ...message };
  }

  @Input() messages: Array<Message>;

  @Input()
  set backLink(backLink: Link) {
    this._backLink = backLink;
  }

  @ViewChild('editor', { read: CodemirrorComponent })
  private editor: CodemirrorComponent;
  @ViewChild('tabs', { read: MatTabGroup }) private tabs: MatTabGroup;

  private _message: Message;
  get message(): Message {
    return this._message;
  }

  readonly options = {
    mode: 'xml',
    lineNumbers: true,
    lineWrapping: true,
    styleActiveLine: true,
    htmlMode: true
  };

  private _backLink: Link | undefined;
  get backLink(): Link {
    if (this._backLink) {
      return this._backLink;
    }

    if (!this.ownerName || !this.projectName) {
      return undefined;
    }

    return {
      routerLink: ['/', this.ownerName, this.projectName],
      name: this.projectName
    };
  }

  constructor(private readonly facade: EditorFacade) {
  }

  ngAfterViewChecked(): void {
    if (this.editor) {
      this.editor.codeMirror.refresh();
    }
    if (this.tabs) {
      this.tabs.realignInkBar();
    }
  }

  @HostListener('keydown.control.enter')
  onSave(): void {
    this.facade.saveMessage(this.message);
  }
}
