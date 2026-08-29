import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-project-infographic',
  templateUrl: './project-infographic.component.svg',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./project-infographic.component.scss']
})
export class ProjectInfographicComponent {
  @Input() contributorCount: number;
  @Input() localeCount: number;
  @Input() keyCount: number;
  @Input() messageCount: number;
}
