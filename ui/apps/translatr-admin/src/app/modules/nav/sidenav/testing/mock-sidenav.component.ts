import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { MatDrawer } from '@angular/material/sidenav';
import { Link } from '@dev/translatr-components';
import { User } from '@dev/translatr-model';

@Component({
  standalone: true,
  selector: 'app-sidenav',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: '<ng-content></ng-content>'
})
export class MockSidenavComponent {
  @Input() page: string;
  @Input() backLink: Link;
  @Input() me: User | undefined;
  @Input() sidenav: MatDrawer;
  @Input() showFooter = true;
  @Input() overlay = false;
  @Input() showLogo = true;
  @Input() headerColor: string | undefined;
}
