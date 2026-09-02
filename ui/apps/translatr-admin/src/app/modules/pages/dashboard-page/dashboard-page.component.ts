import { BreakpointObserver } from '@angular/cdk/layout';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { Route, Router } from '@angular/router';
import { NameIconRoute } from '@translatr/utils';
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { AppFacade } from '../../../+state/app.facade';
import { DASHBOARD_ROUTES } from './dashboard-page.token';

const LARGE_SCREEN = '(min-width: 960px)';

@Component({
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.scss']
})
export class DashboardPageComponent {
  me$ = this.facade.me$;
  children: NameIconRoute[] = this.routes[0].children;

  /**
   * Emits true once the viewport is wide enough (>= 960px, matching the `large`
   * SCSS mixin) to show the sidebar docked beside the content rather than as a
   * toggled overlay. Seeded synchronously so the first render is already correct.
   */
  readonly isLarge$: Observable<boolean> = this.breakpointObserver.observe(LARGE_SCREEN).pipe(
    map(state => state.matches),
    startWith(this.breakpointObserver.isMatched(LARGE_SCREEN))
  );

  constructor(
    private readonly facade: AppFacade,
    private readonly router: Router,
    private readonly breakpointObserver: BreakpointObserver,
    @Inject(DASHBOARD_ROUTES) private routes: NameIconRoute[]
  ) {}

  routerLink(route: Route) {
    if (route.path === '') {
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
