import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, HostBinding, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-metric',
  templateUrl: './metric.component.html',
  styleUrls: ['./metric.component.scss'],
  imports: [CommonModule, RouterModule, MatCardModule, MatIconModule, MatTooltipModule]
})
export class MetricComponent {
  @HostBinding('class.metric') metric = true;
  @Input() routerLink: any[] | string;
  @Input() queryParams: { [p: string]: any } = {};
  @Input() value: any;
  @Input() name: string;
  @Input() icon: string;
}
