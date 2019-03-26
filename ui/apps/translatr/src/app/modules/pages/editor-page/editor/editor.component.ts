import {ChangeDetectionStrategy, Component, HostListener, Input, OnInit, ViewChild} from '@angular/core';
import {Message} from "../../../../shared/message";
import {EditorFacade} from "../+state/editor.facade";
import {MatTabGroup} from "@angular/material";

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditorComponent implements OnInit {
  @Input() title: string;

  private _message: Message;
  get message(): Message {
    return this._message;
  }

  @Input() set message(value: Message) {
    this._message = value;
    if (this.tabs) {
      this.tabs.realignInkBar();
    }
  }

  @Input() messages: Array<Message>;

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

  @HostListener('keydown.control.enter')
  onSave(): void {
    this.facade.saveMessage(this._message);
  }
}
