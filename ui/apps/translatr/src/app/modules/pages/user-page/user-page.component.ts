import { Component, Inject, Injector, OnDestroy } from '@angular/core';
import { ActivatedRoute, CanActivate, Route } from '@angular/router';
import { Feature, User } from '@dev/translatr-model';
import { canActivate$, NameIconRoute } from '@translatr/utils';
import { Observable } from 'rxjs';
import { AppFacade } from '../../../+state/app.facade';
import { UserFacade } from './+state/user.facade';
import { USER_ROUTES } from './user-page.token';

@Component({
  selector: 'app-user-page',
  templateUrl: './user-page.component.html',
  styleUrls: ['./user-page.component.scss']
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
    @Inject(USER_ROUTES) private routes: Array<{ children: NameIconRoute[] }>
  ) {}

  ngOnDestroy(): void {
    this.facade.unload();
  }

  routerLink(user: User | undefined, route: Route): string | undefined {
    if (user === undefined) {
      return undefined;
    }

    if (route === '') {
      return `/${user.username}`;
    }

    return `/${user.username}/${route.path}`;
  }

  canActivate$(route: NameIconRoute): Observable<boolean> {
    return canActivate$(route, this.route, (guard: any) => this.injector.get<CanActivate>(guard));
  }
}
