import { Component, Input, NgModule } from '@angular/core';
import { Link } from '@dev/translatr-components';
import { MockAuthBarItemComponent } from '../auth-bar-item/testing';
import { MatDrawer } from '@angular/material/sidenav';

@Component({
  selector: 'app-navbar',
  template: ''
})
class MockNavbarComponent {
  @Input() title = 'Translatr';
  @Input() page: string;
  @Input() backLink: Link;
  @Input() sidenav: MatDrawer;
  @Input() elevated = true;
  @Input() showLogo = true;
  @Input() overlay = false;
}

@NgModule({
  declarations: [MockNavbarComponent, MockAuthBarItemComponent],
  exports: [MockNavbarComponent, MockAuthBarItemComponent]
})
export class NavbarTestingModule {
}
