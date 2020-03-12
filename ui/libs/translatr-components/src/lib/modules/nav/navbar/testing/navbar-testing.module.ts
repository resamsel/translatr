import { Component, Input, NgModule } from '@angular/core';
import { Link } from '@dev/translatr-components';
import { MockAuthBarItemComponent } from '../auth-bar-item/testing';

@Component({
  selector: 'app-navbar',
  template: ''
})
class MockNavbarComponent {
  @Input() page: string;
  @Input() backLink: Link;
  @Input() elevated = true;
  @Input() overlay = false;
}

@NgModule({
  declarations: [MockNavbarComponent, MockAuthBarItemComponent],
  exports: [MockNavbarComponent, MockAuthBarItemComponent]
})
export class NavbarTestingModule {
}
