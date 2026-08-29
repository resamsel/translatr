import { Component, HostBinding, Input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-empty-view',
  templateUrl: './empty-view.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./empty-view.component.scss']
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
