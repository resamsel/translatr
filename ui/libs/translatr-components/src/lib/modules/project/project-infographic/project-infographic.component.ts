import { Component, Input } from '@angular/core';

@Component({
  selector: 'dev-project-infographic',
  templateUrl: './project-infographic.component.svg',
  styleUrls: ['./project-infographic.component.scss']
})
export class ProjectInfographicComponent {
  @Input() contributorCount: number;
  @Input() localeCount: number;
  @Input() keyCount: number;
  @Input() messageCount: number;
}
