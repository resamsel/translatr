import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { MatDrawer } from '@angular/material/sidenav';
import { Link } from '@dev/translatr-components';

@Component({
  standalone: true,
  selector: 'app-navbar',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: ''
})
export class MockNavbarComponent {
  @Input() title = 'Translatr';
  @Input() page: string;
  @Input() backLink: Link;
  @Input() sidenav: MatDrawer;
  @Input() elevated = true;
  @Input() showLogo = true;
  @Input() overlay = false;
}
