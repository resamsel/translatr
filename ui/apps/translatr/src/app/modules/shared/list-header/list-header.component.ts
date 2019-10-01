import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-list-header',
  templateUrl: './list-header.component.html',
  styleUrls: ['./list-header.component.scss']
})
export class ListHeaderComponent {

  @Input() search: string;
  @Input() addVisible = true;
  @Input() addEnabled = true;
  @Input() addTooltip = 'Create';
  @Input() removeVisible = false;
  @Input() removeEnabled = false;
  @Input() removeTooltip = 'Remove';
  @Output() add = new EventEmitter<void>();
  @Output() remove = new EventEmitter<void>();
  @Output() filter = new EventEmitter<string>();
}
