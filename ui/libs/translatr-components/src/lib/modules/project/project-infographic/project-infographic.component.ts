import { CommonModule } from '@angular/common';
import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  standalone: true,
  selector: 'dev-project-infographic',
  templateUrl: './project-infographic.component.svg',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./project-infographic.component.scss'],
  imports: [CommonModule, TranslocoModule, MatIconModule]
})
export class ProjectInfographicComponent {
  @Input() contributorCount: number;
  @Input() localeCount: number;
  @Input() keyCount: number;
  @Input() messageCount: number;
}
