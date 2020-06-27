import { Component, Input, NgModule } from '@angular/core';

@Component({
  selector: 'dev-error-page',
  template: ''
})
class MockErrorPageComponent {
  @Input() icon: string;
}

@Component({
  selector: 'dev-error-page-header',
  template: ''
})
class MockErrorPageHeaderComponent {}

@Component({
  selector: 'dev-error-page-message',
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
