import { ChangeDetectionStrategy, Component, EventEmitter, HostBinding, HostListener, Input, Output } from '@angular/core';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'confirm-button',
  templateUrl: './confirm-button.component.html',
  styleUrls: ['./confirm-button.component.css']
})
export class ConfirmButtonComponent {
  @Input() tooltip: string;
  @Input() icon: string;
  @Input() disabled = false;
  @Output() readonly confirm = new EventEmitter<void>();

  @HostBinding('class.confirm-button') clazz = true;

  onConfirm(): void {
    this.confirm.emit();
  }

  @HostListener('click', ['$event'])
  onClick(event: MouseEvent): boolean {
    event.stopPropagation();
    event.preventDefault();
    return false;
  }
}
