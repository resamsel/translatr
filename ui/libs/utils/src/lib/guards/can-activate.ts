import { DummyRoute, NameIconRoute } from '../routing';
import { ActivatedRoute, CanActivate } from '@angular/router';
import { combineLatest, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

export const canActivate$ = (
  route: NameIconRoute,
  activatedRoute: ActivatedRoute,
  guardSupplier: (guard: any) => CanActivate
): Observable<boolean> => {
  if (!route.canActivate) {
    return of(true);
  }

  const r = new DummyRoute(
    route,
    activatedRoute.snapshot.params,
    { redirect: false }
  );

  return combineLatest(
    route.canActivate
      .map(guardSupplier)
      .filter((guard: CanActivate) => guard && guard.canActivate)
      .map(
        (guard: CanActivate) =>
          guard.canActivate(r, undefined) as Observable<boolean>
      )
  ).pipe(map((values: boolean[]) => values.every(Boolean)));
};
