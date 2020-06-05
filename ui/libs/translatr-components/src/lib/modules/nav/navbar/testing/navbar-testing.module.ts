import { Component, Input, NgModule } from '@angular/core';
import { MatDrawer } from '@angular/material/sidenav';
import { Link } from '@dev/translatr-components';
import { MockAuthBarItemComponent } from '../auth-bar-item/testing';
import { MockAuthBarLanguageSwitcherComponent } from '../auth-bar-language-switcher/testing';

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
  declarations: [
    MockNavbarComponent,
    MockAuthBarItemComponent,
    MockAuthBarLanguageSwitcherComponent
  ],
  exports: [MockNavbarComponent, MockAuthBarItemComponent, MockAuthBarLanguageSwitcherComponent]
})
export class NavbarTestingModule {}
