import { BreakpointObserver } from '@angular/cdk/layout';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Route, Router, RouterModule } from '@angular/router';
import { FeatureFlagDirective, ThemePreference, ThemeService } from '@dev/translatr-components';
import { Feature } from '@dev/translatr-model';
import { NameIconRoute } from '@translatr/utils';
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { AppFacade } from '../../+state/app.facade';
import { SidenavComponent } from '../nav/sidenav/sidenav.component';
import { DASHBOARD_ROUTES } from '../pages/dashboard-page/dashboard-page.token';

const LARGE_SCREEN = '(min-width: 960px)';

/**
 * Shell mechanics (sidenav drawer + `app-sidenav` navbar/footer) shared by
 * every admin page. A page owns its own instance and decides its navbar
 * accent color ([headerColor], tints the navbar's toolbar row) and body
 * content (default slot) — the shell itself only knows how to navigate
 * between pages.
 */
@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'dev-admin-page',
  templateUrl: './admin-page.component.html',
  styleUrls: ['./admin-page.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    SidenavComponent,
    FeatureFlagDirective,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatListModule
  ]
})
export class AdminPageComponent {
  @Input() page: string | undefined;
  @Input() headerColor: string | undefined;

  readonly Feature = Feature;

  me$ = this.facade.me$;
  children: NameIconRoute[] = this.navItems;

  /**
   * Emits true once the viewport is wide enough (>= 960px, matching the
   * `large` SCSS mixin) to show the sidebar docked beside the content rather
   * than as a toggled overlay. Seeded synchronously so the first render is
   * already correct.
   */
  readonly isLarge$: Observable<boolean> = this.breakpointObserver.observe(LARGE_SCREEN).pipe(
    map(state => state.matches),
    startWith(this.breakpointObserver.isMatched(LARGE_SCREEN))
  );

  constructor(
    private readonly facade: AppFacade,
    private readonly router: Router,
    private readonly breakpointObserver: BreakpointObserver,
    @Inject(DASHBOARD_ROUTES) private readonly navItems: NameIconRoute[],
    private readonly themeService: ThemeService
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
    const activeRoutes = this.children.filter(route => this.isLinkActive(this.routerLink(route)));
    if (activeRoutes.length === 0) {
      return undefined;
    }

    return activeRoutes[0].data.name;
  }

  onThemeChange(preference: ThemePreference): void {
    this.themeService.setPreference(preference);
  }
}
