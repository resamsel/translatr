import { Component, Input, NgModule } from '@angular/core';
import { Link } from '@dev/translatr-components';
import { User } from '@dev/translatr-model';

@Component({
  selector: 'app-sidenav',
  template: ''
})
class MockSidenavComponent {
  @Input() page: string;
  @Input() backLink: Link;
  @Input() me: User | undefined;
  @Input() elevated = true;
  @Input() overlay = false;
  @Input() showDashboardLink = false;
}

@NgModule({
  declarations: [MockSidenavComponent],
  exports: [MockSidenavComponent]
})
export class SidenavTestingModule {
}
