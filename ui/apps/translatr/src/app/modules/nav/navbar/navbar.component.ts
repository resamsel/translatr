import {Component, Input, OnInit} from '@angular/core';
import {MatDrawer} from "@angular/material";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {

  @Input() sidenav: MatDrawer;
  @Input() backLink: {routerLink: string[]; name: string};
  @Input() page: string;

  constructor() { }

  ngOnInit() {
  }

}
