import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: true,
  selector: 'dev-error-page',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockErrorPageComponent {
  @Input() icon: string;
}

@Component({
  standalone: true,
  selector: 'dev-error-page-header',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockErrorPageHeaderComponent {}

@Component({
  standalone: true,
  selector: 'dev-error-page-message',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockErrorPageMessageComponent {}
