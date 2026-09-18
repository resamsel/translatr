import { CommonModule } from '@angular/common';
import { Component, HostBinding, Input, ChangeDetectionStrategy } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  standalone: true,
  selector: 'dev-empty-view',
  templateUrl: './empty-view.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./empty-view.component.scss'],
  imports: [CommonModule, MatIconModule]
})
export class EmptyViewComponent {
  @Input() icon: string;
  @HostBinding('class')
  @Input()
  justifyContent: 'start' | 'center' = 'start';
  @HostBinding('class')
  @Input()
  alignment: 'horizontal' | 'vertical' = 'vertical';
}
