import { Component, HostBinding, Input } from '@angular/core';

@Component({
  selector: 'dev-empty-view',
  templateUrl: './empty-view.component.html',
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
