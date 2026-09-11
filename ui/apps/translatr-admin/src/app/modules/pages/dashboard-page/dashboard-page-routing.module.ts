import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../../../guards/auth.guard';
import { AccessTokensComponent } from '../access-tokens/access-tokens.component';
import { FeatureFlagsComponent } from '../feature-flags/feature-flags.component';
import { GlobalFeatureFlagsComponent } from '../global-feature-flags/global-feature-flags.component';
import { HealthComponent } from '../health/health.component';
import { InfoComponent } from '../info/info.component';
import { DASHBOARD_ROUTES } from './dashboard-page.token';
import { ProjectsComponent } from '../projects/projects.component';
import { UserComponent } from '../user/user.component';
import { UsersComponent } from '../users/users.component';

// Every admin page owns its own `dev-admin-page` shell instance (an
// `AdminPageComponent`), so each sits at the top level rather than nested
// under one shared shell/router-outlet. `canActivate` guards each page;
// `canActivateChild` additionally covers a page's own child routes (e.g.
// Users' `:id` detail).
export const routes: Routes = [
  {
    component: InfoComponent,
    path: '',
    canActivate: [AuthGuard],
    data: {
      icon: 'view_quilt',
      name: 'Dashboard',
    },
  },
  {
    path: 'users',
    canActivate: [AuthGuard],
    canActivateChild: [AuthGuard],
    data: {
      icon: 'group',
      name: 'Users',
    },
    children: [
      {
        component: UsersComponent,
        path: '',
        pathMatch: 'full',
      },
      {
        component: UserComponent,
        path: ':id',
      },
    ],
  },
  {
    component: ProjectsComponent,
    path: 'projects',
    canActivate: [AuthGuard],
    data: {
      icon: 'library_books',
      name: 'Projects',
    },
  },
  {
    component: AccessTokensComponent,
    path: 'accesstokens',
    canActivate: [AuthGuard],
    data: {
      icon: 'vpn_key',
      name: 'Access Tokens',
    },
  },
  {
    component: FeatureFlagsComponent,
    path: 'featureflags',
    canActivate: [AuthGuard],
    data: {
      icon: 'flag',
      name: 'Feature Flags',
    },
  },
  {
    component: GlobalFeatureFlagsComponent,
    path: 'featureflags/global',
    canActivate: [AuthGuard],
    data: {
      icon: 'public',
      name: 'Global Feature Flags',
    },
  },
  {
    component: HealthComponent,
    path: 'health',
    canActivate: [AuthGuard],
    data: {
      icon: 'health_and_safety',
      name: 'Health',
    },
  },
  // Any admin URL that matches no page falls back to the guarded dashboard
  // rather than leaving the router with no active page. It belongs here (the
  // last admin feature module loaded) so it sorts after the real routes
  // above; a wildcard in the root routing module would shadow every one.
  { path: '**', redirectTo: '' },
];

// Nav-list source for every admin page's sidenav drawer: every real page
// route above, excluding the trailing wildcard fallback (it carries no
// `data.icon`/`data.name`).
const NAV_ITEMS: Routes = routes.filter(route => route.path !== '**');

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [{ provide: DASHBOARD_ROUTES, useValue: NAV_ITEMS }]
})
export class DashboardPageRoutingModule {}
