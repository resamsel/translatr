import { Component, Input, NgModule, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-empty-view',
  changeDetection: ChangeDetectionStrategy.Eager,
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
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
class MockEmptyViewActionsComponent {}

@Component({
  standalone: false,
  selector: 'dev-empty-view-content',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
class MockEmptyViewContentComponent {}

@Component({
  standalone: false,
  selector: 'dev-empty-view-header',
  changeDetection: ChangeDetectionStrategy.Eager,
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
