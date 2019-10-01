import { Component, Input } from '@angular/core';
import { MatDrawer } from '@angular/material/sidenav';

export interface Link {
  routerLink: string[];
  name?: string;
}

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {
  @Input() title = 'Translatr';
  @Input() sidenav: MatDrawer;
  @Input() backLink: Link;
  @Input() page: string;
  @Input() elevated = true;
}
