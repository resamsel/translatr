import { ActivatedRouteSnapshot, Data, Params } from '@angular/router';

export class DummyRoute extends ActivatedRouteSnapshot {
  constructor(
    public readonly routeConfig: any,
    public readonly params: Params,
    public readonly data: Data
  ) {
    super();
  }
}
