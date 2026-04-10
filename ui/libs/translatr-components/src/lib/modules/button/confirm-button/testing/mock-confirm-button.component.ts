import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  standalone: false,
  selector: 'confirm-button',
  template: ''
})
export class MockConfirmButtonComponent {
  @Input() tooltip: string;
  @Input() icon: string;
  @Input() disabled = false;
  @Output() readonly confirm = new EventEmitter<void>();
}
