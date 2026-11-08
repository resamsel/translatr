import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  standalone: true,
  selector: 'dev-admin-page',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: '<ng-content></ng-content>'
})
export class MockAdminPageComponent {
  @Input() page: string | undefined;
  @Input() headerColor: string | undefined;
}
