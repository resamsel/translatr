import { CommonModule } from '@angular/common';
import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  standalone: true,
  selector: 'dev-error-page',
  templateUrl: './error-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./error-page.component.scss'],
  imports: [CommonModule, MatIconModule]
})
export class ErrorPageComponent {
  @Input() icon: string;
}
