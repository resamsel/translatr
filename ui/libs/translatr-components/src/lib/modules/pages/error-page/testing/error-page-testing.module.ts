import { Component, Input, NgModule, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-error-page',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
class MockErrorPageComponent {
  @Input() icon: string;
}

@Component({
  standalone: false,
  selector: 'dev-error-page-header',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
class MockErrorPageHeaderComponent {}

@Component({
  standalone: false,
  selector: 'dev-error-page-message',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
class MockErrorPageMessageComponent {}

@NgModule({
  declarations: [
    MockErrorPageComponent,
    MockErrorPageHeaderComponent,
    MockErrorPageMessageComponent
  ],
  exports: [MockErrorPageComponent, MockErrorPageHeaderComponent, MockErrorPageMessageComponent]
})
export class ErrorPageTestingModule {}
