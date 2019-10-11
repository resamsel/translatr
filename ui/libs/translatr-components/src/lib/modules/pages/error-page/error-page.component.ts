import { Component, Input } from '@angular/core';

@Component({
  selector: 'dev-error-page',
  templateUrl: './error-page.component.html',
  styleUrls: ['./error-page.component.scss']
})
export class ErrorPageComponent {
  @Input() icon: string;
}
