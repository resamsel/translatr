import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { Route, Router } from '@angular/router';
import { NameIconRoute } from '@translatr/utils';
import { AppFacade } from '../../../+state/app.facade';
import { DASHBOARD_ROUTES } from './dashboard-page.token';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.scss']
})
export class DashboardPageComponent {
  me$ = this.facade.me$;
  children: NameIconRoute[] = this.routes[0].children;

  constructor(
    private readonly facade: AppFacade,
    private readonly router: Router,
    @Inject(DASHBOARD_ROUTES) private routes: NameIconRoute[]
  ) {}

  routerLink(route: Route) {
    if (route === '') {
      return '/';
    }

    return `/${route.path}`;
  }

  isLinkActive(url) {
    const charPos = this.router.url.indexOf('?');
    const cleanUrl = charPos !== -1 ? this.router.url.slice(0, charPos) : this.router.url;
    return cleanUrl === url;
  }

  activePage(): string | undefined {
    if (this.children === undefined) {
      return undefined;
    }

    const activeRoutes = this.children.filter(route => this.isLinkActive(this.routerLink(route)));
    if (activeRoutes.length === 0) {
      return undefined;
    }

    return activeRoutes[0].data.name;
  }
}
