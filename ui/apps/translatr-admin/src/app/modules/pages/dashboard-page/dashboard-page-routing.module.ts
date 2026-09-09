import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../../../guards/auth.guard';
import { AccessTokensComponent } from '../access-tokens/access-tokens.component';
import { FeatureFlagsPageComponent } from '../feature-flags-page/feature-flags-page.component';
import { FeatureFlagsComponent } from '../feature-flags/feature-flags.component';
import { GlobalFeatureFlagsComponent } from '../global-feature-flags/global-feature-flags.component';
import { HealthComponent } from '../health/health.component';
import { InfoComponent } from '../info/info.component';
import { DashboardPageComponent } from './dashboard-page.component';
import { DASHBOARD_ROUTES } from './dashboard-page.token';
import { ProjectsComponent } from '../projects/projects.component';
import { UserComponent } from '../user/user.component';
import { UsersComponent } from '../users/users.component';

export const routes: Routes = [
  {
    component: DashboardPageComponent,
    path: '',
    // canActivateChild keeps every current and future descendant page admin-only
    // without each route having to opt in; canActivate covers the shell itself.
    canActivate: [AuthGuard],
    canActivateChild: [AuthGuard],
    children: [
      {
        component: InfoComponent,
        path: '',
        data: {
          icon: 'view_quilt',
          name: 'Dashboard'
        }
      },
      {
        path: 'users',
        data: {
          icon: 'group',
          name: 'Users'
        },
        children: [
          {
            component: UsersComponent,
            path: '',
            pathMatch: 'full'
          },
          {
            component: UserComponent,
            path: ':id'
          }
        ]
      },
      {
        component: ProjectsComponent,
        path: 'projects',
        data: {
          icon: 'library_books',
          name: 'Projects'
        }
      },
      {
        component: AccessTokensComponent,
        path: 'accesstokens',
        data: {
          icon: 'vpn_key',
          name: 'Access Tokens'
        }
      },
      {
        component: HealthComponent,
        path: 'health',
        data: {
          icon: 'health_and_safety',
          name: 'Health'
        }
      },
      {
        component: FeatureFlagsPageComponent,
        path: 'featureflags',
        data: {
          icon: 'flag',
          name: 'Feature Flags'
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'user'
          },
          {
            component: FeatureFlagsComponent,
            path: 'user',
            data: {
              icon: 'person',
              name: 'featureFlags.tab.user'
            }
          },
          {
            component: GlobalFeatureFlagsComponent,
            path: 'global',
            data: {
              icon: 'public',
              name: 'featureFlags.tab.global'
            }
          }
        ]
      }
    ]
  },
  // Any admin URL that matches no page falls back to the guarded shell rather
  // than leaving the router with no active page. It belongs here (the last admin
  // feature module loaded) so it sorts after the shell's own `path: ''` route;
  // a wildcard in the root routing module would shadow every real route.
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [{ provide: DASHBOARD_ROUTES, useValue: routes }]
})
export class DashboardPageRoutingModule {}
