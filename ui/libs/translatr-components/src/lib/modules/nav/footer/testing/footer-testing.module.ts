import { Component, NgModule, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-footer',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
class MockFooterComponent {}

@NgModule({
  declarations: [MockFooterComponent],
  exports: [MockFooterComponent]
})
export class FooterTestingModule {}
