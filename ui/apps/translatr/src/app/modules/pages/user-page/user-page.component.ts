import { Component, Inject, Injector, OnInit } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot, CanActivate, Route } from '@angular/router';
import { User } from '@dev/translatr-model';
import { NameIconRoute } from '@translatr/utils';
import { USER_ROUTES } from './user-page.token';
import { AppFacade } from '../../../+state/app.facade';
import { combineLatest, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

class DummyRoute extends ActivatedRouteSnapshot {
  constructor(public readonly routeConfig: any) {
    super();
  }
}

@Component({
  selector: 'app-user-page',
  templateUrl: './user-page.component.html',
  styleUrls: ['./user-page.component.scss']
})
export class UserPageComponent implements OnInit {
  me$ = this.appFacade.me$;
  user: User;
  children: NameIconRoute[] = this.routes[0].children;

  constructor(
    private readonly appFacade: AppFacade,
    private readonly injector: Injector,
    private readonly route: ActivatedRoute,
    @Inject(USER_ROUTES) private routes: { children: NameIconRoute[] }[]
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe((data: { user: User }) => {
      this.user = data.user;
    });
  }

  routerLink(route: Route) {
    if (route === '') {
      return `/${this.user.username}`;
    }

    return `/${this.user.username}/${route.path}`;
  }

  canActivate$(route: NameIconRoute): Observable<boolean> {
    if (!route.canActivate) {
      return of(true);
    }

    const r = new DummyRoute(route);

    return combineLatest(
      route.canActivate
        .map((guard: any) => this.injector.get<CanActivate>(guard))
        .filter((guard: CanActivate) => guard && guard.canActivate)
        .map(
          (guard: CanActivate) =>
            guard.canActivate(r, undefined) as Observable<boolean>
        )
    ).pipe(map((values: boolean[]) => values.every(Boolean)));
  }
}
