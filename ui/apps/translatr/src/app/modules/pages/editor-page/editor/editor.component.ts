import { AfterViewChecked, ChangeDetectionStrategy, Component, HostListener, Input, ViewChild } from '@angular/core';
import { Message, User } from '@dev/translatr-model';
import { EditorFacade } from '../+state/editor.facade';
import { MatTabGroup } from '@angular/material/tabs';
import { CodemirrorComponent } from '@ctrl/ngx-codemirror';
import { Link } from '@dev/translatr-components';

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
  @Input() message: Message;

  @Input() messages: Array<Message>;

  @Input()
  set backLink(backLink: Link) {
    this._backLink = backLink;
  }

  @ViewChild('editor', { read: CodemirrorComponent, static: false })
  private editor: CodemirrorComponent;
  @ViewChild('tabs', { read: MatTabGroup, static: true }) private tabs: MatTabGroup;

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
