import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-list-header',
  templateUrl: './list-header.component.html',
  styleUrls: ['./list-header.component.scss']
})
export class ListHeaderComponent {

  @Input() search: string;
  @Input() searchEnabled = true;
  @Input() addVisible = true;
  @Input() addEnabled = true;
  @Input() addTooltip = 'Create';
  @Input() removeVisible = false;
  @Input() removeEnabled = false;
  @Input() removeTooltip = 'Remove';
  @Output() readonly add = new EventEmitter<void>();
  @Output() readonly remove = new EventEmitter<void>();
  @Output() readonly filter = new EventEmitter<string>();

  constructor() {
    this.add.subscribe(() => console.log('ListHeader.add'));
  }
}
