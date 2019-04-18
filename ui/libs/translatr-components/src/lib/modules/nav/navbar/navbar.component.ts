import {Component, Input, OnInit} from '@angular/core';
import {MatDrawer} from "@angular/material";
import {User} from "@dev/translatr-model";

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
  @Input() me: User | undefined;

  constructor() {
  }

  ngOnInit() {
  }

}
