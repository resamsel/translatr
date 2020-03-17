import { Component, Input, NgModule } from '@angular/core';

@Component({
  selector: 'dev-empty-view',
  template: ''
})
class MockEmptyViewComponent {
  @Input() icon: string;
  @Input() justifyContent: 'start' | 'center' = 'start';
  @Input() alignment: 'horizontal' | 'vertical' = 'horizontal';
}

@NgModule({
  declarations: [MockEmptyViewComponent],
  exports: [MockEmptyViewComponent]
})
export class EmptyViewTestingModule {
}
