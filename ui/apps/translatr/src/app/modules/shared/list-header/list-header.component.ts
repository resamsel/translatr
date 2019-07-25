import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-list-header',
  templateUrl: './list-header.component.html',
  styleUrls: ['./list-header.component.scss']
})
export class ListHeaderComponent {

  @Input() addEnabled = true;
  @Input() removeEnabled = false;
  @Output() add = new EventEmitter<void>();
  @Output() remove = new EventEmitter<void>();
}
