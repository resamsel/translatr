import { Component, Inject, Injector, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, ActivatedRouteSnapshot, CanActivate, Data, Params, Route } from '@angular/router';
import { User } from '@dev/translatr-model';
import { NameIconRoute } from '@translatr/utils';
import { USER_ROUTES } from './user-page.token';
import { AppFacade } from '../../../+state/app.facade';
import { combineLatest, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserFacade } from './+state/user.facade';

class DummyRoute extends ActivatedRouteSnapshot {
  constructor(
    public readonly routeConfig: any,
    public readonly params: Params,
    public readonly data: Data
  ) {
    super();
  }
}

@Component({
  selector: 'app-user-page',
  templateUrl: './user-page.component.html',
  styleUrls: ['./user-page.component.scss']
})
export class UserPageComponent implements OnInit, OnDestroy {
  readonly me$ = this.appFacade.me$;
  readonly user$ = this.facade.user$;

  children: NameIconRoute[] = this.routes[0].children;

  constructor(
    private readonly appFacade: AppFacade,
    private readonly facade: UserFacade,
    private readonly injector: Injector,
    private readonly route: ActivatedRoute,
    @Inject(USER_ROUTES) private routes: { children: NameIconRoute[] }[]
  ) {
  }

  ngOnInit(): void {
    this.route.params.subscribe((params: Params) =>
      this.facade.loadUser(params.username));
  }

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
    if (!route.canActivate) {
      return of(true);
    }

    const r = new DummyRoute(
      route,
      this.route.snapshot.params,
      { redirect: false }
    );

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
