import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-metric',
  templateUrl: './metric.component.html',
  styleUrls: ['./metric.component.scss']
})
export class MetricComponent {
  @HostBinding('class.metric') metric = true;
  @Input() routerLink: any[] | string;
  @Input() value: any;
  @Input() name: string;
  @Input() icon: string;
}
