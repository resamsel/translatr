import {Component, Inject, OnInit} from '@angular/core';
import {AppFacade} from "../../../+state/app.facade";
import {Route, Routes} from "@angular/router";
import {DASHBOARD_ROUTES} from "./dashboard-page.token";

interface NameIconRoute {
  data: {
    icon: string;
    name: string;
  };
}

@Component({
  selector: 'dev-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.css']
})
export class DashboardPageComponent implements OnInit {
  me$ = this.facade.me$;
  children: NameIconRoute[] = this.routes[0].children;

  constructor(
    private readonly facade: AppFacade,
    @Inject(DASHBOARD_ROUTES) private routes: {children: NameIconRoute[]}[]
  ) {
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
