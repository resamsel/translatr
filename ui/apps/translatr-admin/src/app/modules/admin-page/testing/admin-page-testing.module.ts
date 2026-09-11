import { ChangeDetectionStrategy, Component, Input, NgModule } from '@angular/core';

@Component({
  standalone: false,
  selector: 'dev-admin-page',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: '<ng-content></ng-content>'
})
class MockAdminPageComponent {
  @Input() page: string | undefined;
  @Input() headerColor: string | undefined;
}

@NgModule({
  declarations: [MockAdminPageComponent],
  exports: [MockAdminPageComponent]
})
export class AdminPageTestingModule {}
