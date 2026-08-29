import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-error-page',
  templateUrl: './error-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./error-page.component.scss']
})
export class ErrorPageComponent {
  @Input() icon: string;
}
