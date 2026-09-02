import { Component, Input, NgModule, ChangeDetectionStrategy } from '@angular/core';
import { MatDrawer } from '@angular/material/sidenav';
import { Link } from '@dev/translatr-components';
import { User } from '@dev/translatr-model';

@Component({
  standalone: false,
  selector: 'app-sidenav',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: '<ng-content></ng-content>'
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
export class SidenavTestingModule {}
