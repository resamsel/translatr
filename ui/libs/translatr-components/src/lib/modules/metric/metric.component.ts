import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'metric',
  templateUrl: './metric.component.html',
  styleUrls: ['./metric.component.scss'],
  host: {
    class: 'metric'
  }
})
export class MetricComponent {
  @Input() routerLink: any[] | string;
  @Input() value: any;
  @Input() name: string;
  @Input() icon: string;
}
