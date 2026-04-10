import { Component, Input, NgModule } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-empty-view',
  template: ''
})
class MockEmptyViewComponent {
  @Input() icon: string;
  @Input() justifyContent: 'start' | 'center' = 'start';
  @Input() alignment: 'horizontal' | 'vertical' = 'horizontal';
}

@Component({
  standalone: false,
  selector: 'dev-empty-view-actions',
  template: ''
})
class MockEmptyViewActionsComponent {}

@Component({
  standalone: false,
  selector: 'dev-empty-view-content',
  template: ''
})
class MockEmptyViewContentComponent {}

@Component({
  standalone: false,
  selector: 'dev-empty-view-header',
  template: ''
})
class MockEmptyViewHeaderComponent {}

@NgModule({
  declarations: [
    MockEmptyViewComponent,
    MockEmptyViewActionsComponent,
    MockEmptyViewContentComponent,
    MockEmptyViewHeaderComponent
  ],
  exports: [
    MockEmptyViewComponent,
    MockEmptyViewActionsComponent,
    MockEmptyViewContentComponent,
    MockEmptyViewHeaderComponent
  ]
})
export class EmptyViewTestingModule {}
