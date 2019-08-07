import { Component, Input, OnInit } from '@angular/core';
import { MatDrawer } from '@angular/material/sidenav';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  @Input() title = 'Translatr';
  @Input() sidenav: MatDrawer;
  @Input() backLink: { routerLink: string[]; name: string };
  @Input() page: string;

  constructor() {}

  ngOnInit() {}
}
