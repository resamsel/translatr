import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'confirm-button',
  templateUrl: './confirm-button.component.html',
  styleUrls: ['./confirm-button.component.css']
})
export class ConfirmButtonComponent {

  @Input() title: string;
  @Input() icon: string;
  @Input() disabled = false;
  @Output() readonly confirm = new EventEmitter<void>();

  onConfirm() {
    this.confirm.emit();
  }
}
