import { Component, Input, ChangeDetectionStrategy } from '@angular/core';

@Component({
  standalone: true,
  selector: 'dev-empty-view',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockEmptyViewComponent {
  @Input() icon: string;
  @Input() justifyContent: 'start' | 'center' = 'start';
  @Input() alignment: 'horizontal' | 'vertical' = 'horizontal';
}

@Component({
  standalone: true,
  selector: 'dev-empty-view-actions',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockEmptyViewActionsComponent {}

@Component({
  standalone: true,
  selector: 'dev-empty-view-content',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockEmptyViewContentComponent {}

@Component({
  standalone: true,
  selector: 'dev-empty-view-header',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockEmptyViewHeaderComponent {}
