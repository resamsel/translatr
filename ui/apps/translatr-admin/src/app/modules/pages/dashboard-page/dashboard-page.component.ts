import {Component, OnInit} from '@angular/core';
import {AppFacade} from "../../../+state/app.facade";
import {routes} from "./dashboard-page-routing.module";
import {Route} from "@angular/router";

@Component({
  selector: 'dev-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.css']
})
export class DashboardPageComponent implements OnInit {
  me$ = this.facade.me$;
  routes = routes[0].children;

  constructor(private readonly facade: AppFacade) {
  }

  ngOnInit() {
  }

  routerLink(route: Route) {
    if (route === '') {
      return '/';
    }

    return `/${route.path}`;
  }
}
