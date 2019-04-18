import {
  AfterViewChecked,
  ChangeDetectionStrategy,
  Component,
  HostListener,
  Input,
  OnInit,
  ViewChild
} from '@angular/core';
import {Message, User} from "@dev/translatr-model";
import {EditorFacade} from "../+state/editor.facade";
import {MatTabGroup} from "@angular/material";
import {CodemirrorComponent} from "@ctrl/ngx-codemirror";

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditorComponent implements OnInit, AfterViewChecked {
  @Input() me: User;
  @Input() ownerName: string;
  @Input() projectName: string;
  @Input() name: string;
  @Input() message: Message;
  @Input() messages: Array<Message>;

  @ViewChild('editor', {read: CodemirrorComponent}) private editor: CodemirrorComponent;
  @ViewChild('tabs', {read: MatTabGroup}) private tabs: MatTabGroup;

  readonly options = {
    mode: 'xml',
    lineNumbers: true,
    lineWrapping: true,
    styleActiveLine: true,
    htmlMode: true
  };

  constructor(private readonly facade: EditorFacade) {
  }

  ngOnInit() {
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
