import { Component, Input, NgModule } from '@angular/core';
import { Link } from '@dev/translatr-components';
import { User } from '@dev/translatr-model';
import { MatDrawer } from '@angular/material/sidenav';

@Component({
  selector: 'app-sidenav',
  template: ''
})
class MockSidenavComponent {
  @Input() page: string;
  @Input() backLink: Link;
  @Input() me: User | undefined;
  @Input() sidenav: MatDrawer;
  @Input() showFooter = true;
  @Input() overlay = false;
}

@NgModule({
  declarations: [MockSidenavComponent],
  exports: [MockSidenavComponent]
})
export class SidenavTestingModule {
}
