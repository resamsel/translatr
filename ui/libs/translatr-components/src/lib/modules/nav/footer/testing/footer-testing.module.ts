import { Component, NgModule } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-footer',
  template: ''
})
class MockFooterComponent {}

@NgModule({
  declarations: [MockFooterComponent],
  exports: [MockFooterComponent]
})
export class FooterTestingModule {}
