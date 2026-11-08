import { CommonModule } from '@angular/common';
import { Component, Inject, Injector, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, CanActivate, Route, RouterModule } from '@angular/router';
import { FeatureFlagClassDirective } from '@dev/translatr-components';
import { Feature, User } from '@dev/translatr-model';
import { TranslocoModule } from '@jsverse/transloco';
import { canActivate$, NameIconRoute } from '@translatr/utils';
import { Observable } from 'rxjs';
import { AppFacade } from '../../../+state/app.facade';
import { SidenavComponent } from '../../nav/sidenav/sidenav.component';
import { UserFacade } from './+state/user.facade';
import { USER_ROUTES } from './user-page.token';

@Component({
  standalone: true,
  selector: 'app-user-page',
  templateUrl: './user-page.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./user-page.component.scss'],
  imports: [CommonModule, RouterModule, TranslocoModule, SidenavComponent, MatIconModule, MatTabsModule, FeatureFlagClassDirective]
})
export class UserPageComponent implements OnDestroy {
  readonly me$ = this.appFacade.me$;
  readonly user$ = this.facade.user$;

  children: NameIconRoute[] = this.routes[0].children;

  readonly Feature = Feature;

  constructor(
    private readonly appFacade: AppFacade,
    private readonly facade: UserFacade,
    private readonly injector: Injector,
    private readonly route: ActivatedRoute,
    @Inject(USER_ROUTES) private routes: { children: NameIconRoute[] }[]
  ) {}

  ngOnDestroy(): void {
    this.facade.unload();
  }

  routerLink(user: User | undefined, route: Route): string | undefined {
    if (user === undefined) {
      return undefined;
    }

    if (route.path === '') {
      return `/${user.username}`;
    }

    return `/${user.username}/${route.path}`;
  }

  canActivate$(route: NameIconRoute): Observable<boolean> {
    return canActivate$(route, this.route, (guard: any) => this.injector.get<CanActivate>(guard));
  }
}
